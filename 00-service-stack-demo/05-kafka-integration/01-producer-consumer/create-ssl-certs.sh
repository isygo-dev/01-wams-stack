#!/bin/bash

# Set variables
CERTS_DIR="./certs"
PASSWORD="changeit"
VALIDITY_DAYS=365
CA_KEY="ca-key.pem"
CA_CERT="ca-cert.pem"

# Enable debugging
set -x

# Create certs directory if it doesn't exist
mkdir -p "$CERTS_DIR" || { echo "Failed to create certs directory"; exit 1; }

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check for required tools
if ! command_exists keytool || ! command_exists openssl; then
    echo "Error: 'keytool' and 'openssl' are required. Please install them (e.g., via OpenJDK and OpenSSL packages)."
    exit 1
fi

# Remove existing certificates to avoid conflicts
rm -f "$CERTS_DIR"/*.pem "$CERTS_DIR"/*.jks "$CERTS_DIR"/*.csr "$CERTS_DIR"/*.cer

# Generate CA key and certificate
echo "Generating CA key and certificate..."
openssl genrsa -out "$CERTS_DIR/$CA_KEY" 2048 || { echo "Failed to generate CA key"; exit 1; }
MSYS_NO_PATHCONV=1 openssl req -x509 -new -nodes -key "$CERTS_DIR/$CA_KEY" -sha256 -days "$VALIDITY_DAYS" -out "$CERTS_DIR/$CA_CERT" \
    -subj "/C=US/ST=State/L=City/O=Organization/OU=Unit/CN=TestCA" || { echo "Failed to generate CA cert"; exit 1; }

# Function to generate certificates and keystores
generate_cert() {
    local name=$1
    local cn=$2

    # Generate keypair and CSR
    echo "Generating certificate for $name..."
    keytool -genkeypair -alias "$name" -keyalg RSA -keysize 2048 -dname "CN=$cn,OU=Unit,O=Organization,L=City,ST=State,C=US" \
        -validity "$VALIDITY_DAYS" -keystore "$CERTS_DIR/$name.keystore.jks" -storepass "$PASSWORD" -keypass "$PASSWORD" || { echo "Failed to generate keystore for $name"; exit 1; }
    keytool -certreq -alias "$name" -keystore "$CERTS_DIR/$name.keystore.jks" -file "$CERTS_DIR/$name.csr" \
        -storepass "$PASSWORD" || { echo "Failed to generate CSR for $name"; exit 1; }

    # Sign CSR with CA
    MSYS_NO_PATHCONV=1 openssl x509 -req -in "$CERTS_DIR/$name.csr" -CA "$CERTS_DIR/$CA_CERT" -CAkey "$CERTS_DIR/$CA_KEY" \
        -CAcreateserial -out "$CERTS_DIR/$name.cer" -days "$VALIDITY_DAYS" -sha256 || { echo "Failed to sign CSR for $name"; exit 1; }

    # Import CA certificate into keystore
    keytool -import -trustcacerts -alias CARoot -file "$CERTS_DIR/$CA_CERT" -keystore "$CERTS_DIR/$name.keystore.jks" \
        -storepass "$PASSWORD" -noprompt || { echo "Failed to import CA cert into keystore for $name"; exit 1; }

    # Import signed certificate into keystore
    keytool -import -alias "$name" -file "$CERTS_DIR/$name.cer" -keystore "$CERTS_DIR/$name.keystore.jks" \
        -storepass "$PASSWORD" -noprompt || { echo "Failed to import signed cert into keystore for $name"; exit 1; }

    # Create truststore and import CA certificate
    keytool -import -trustcacerts -alias CARoot -file "$CERTS_DIR/$CA_CERT" -keystore "$CERTS_DIR/$name.truststore.jks" \
        -storepass "$PASSWORD" -noprompt || { echo "Failed to create truststore for $name"; exit 1; }
}

# Generate certificates for Zookeeper, Kafka, and Client
generate_cert "zookeeper" "zookeeper"
generate_cert "kafka" "kafka"
generate_cert "client" "client"

# Verify truststores exist
for name in zookeeper kafka client; do
    if [ ! -f "$CERTS_DIR/$name.truststore.jks" ]; then
        echo "Error: Truststore $name.truststore.jks was not created."
        exit 1
    fi
done

# Clean up temporary files
rm -f "$CERTS_DIR"/*.csr "$CERTS_DIR"/*.cer "$CERTS_DIR/$CA_KEY" "$CERTS_DIR/$CA_CERT" "$CERTS_DIR/ca.srl"

echo "Certificates and truststores generated in $CERTS_DIR:"
ls -l "$CERTS_DIR"
echo "Done. Ensure the password 'changeit' is used in your docker-compose.yml and application.yml."