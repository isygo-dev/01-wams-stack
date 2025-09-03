#!/bin/bash

# Set variables
CERTS_DIR="./certs"
CONFIG_DIR="./config"
PASSWORD="changeit"
VALIDITY_DAYS=365
CA_KEY="ca-key.pem"
CA_CERT="ca-cert.pem"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Enable debugging
set -e

echo -e "${GREEN}🔐 Kafka SSL Certificate Generator${NC}"
echo "========================================="

# Create directories if they don't exist
mkdir -p "$CERTS_DIR" "$CONFIG_DIR" || { echo -e "${RED}Failed to create directories${NC}"; exit 1; }

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check for required tools
echo -e "${YELLOW}Checking required tools...${NC}"
if ! command_exists keytool || ! command_exists openssl; then
    echo -e "${RED}Error: 'keytool' and 'openssl' are required. Please install them.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Required tools found${NC}"

# Remove existing certificates to avoid conflicts
echo -e "${YELLOW}Cleaning up old certificates...${NC}"
rm -f "$CERTS_DIR"/*.pem "$CERTS_DIR"/*.jks "$CERTS_DIR"/*.csr "$CERTS_DIR"/*.cer "$CERTS_DIR"/*.srl

# Generate CA key and certificate
echo -e "${YELLOW}Generating CA key and certificate...${NC}"
openssl genrsa -out "$CERTS_DIR/$CA_KEY" 2048 || { echo -e "${RED}Failed to generate CA key${NC}"; exit 1; }

# Create CA config file for better certificate generation
cat > "$CERTS_DIR/ca.conf" << EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_ca
prompt = no

[req_distinguished_name]
C = US
ST = California
L = San Francisco
O = Kafka Test CA
OU = IT Department
CN = Kafka Test CA

[v3_ca]
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = CA:true
keyUsage = keyCertSign, cRLSign
EOF

openssl req -x509 -new -nodes -key "$CERTS_DIR/$CA_KEY" -sha256 -days "$VALIDITY_DAYS" \
    -out "$CERTS_DIR/$CA_CERT" -config "$CERTS_DIR/ca.conf" -extensions v3_ca || {
    echo -e "${RED}Failed to generate CA cert${NC}"; exit 1;
}
echo -e "${GREEN}✓ CA certificate generated${NC}"

# Function to generate certificates and keystores with SAN
generate_cert() {
    local name=$1
    local cn=$2
    local san=$3

    echo -e "${YELLOW}Generating certificate for $name...${NC}"

    # Create config file for certificate with SAN
    cat > "$CERTS_DIR/${name}.conf" << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
C = US
ST = California
L = San Francisco
O = Kafka Test
OU = IT Department
CN = $cn

[v3_req]
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = $san
EOF

    # Generate keypair and CSR with SAN
    keytool -genkeypair -alias "$name" -keyalg RSA -keysize 2048 \
        -dname "CN=$cn,OU=IT Department,O=Kafka Test,L=San Francisco,ST=California,C=US" \
        -validity "$VALIDITY_DAYS" -keystore "$CERTS_DIR/$name.keystore.jks" \
        -storepass "$PASSWORD" -keypass "$PASSWORD" || {
        echo -e "${RED}Failed to generate keystore for $name${NC}"; exit 1;
    }

    keytool -certreq -alias "$name" -keystore "$CERTS_DIR/$name.keystore.jks" \
        -file "$CERTS_DIR/$name.csr" -storepass "$PASSWORD" || {
        echo -e "${RED}Failed to generate CSR for $name${NC}"; exit 1;
    }

    # Sign CSR with CA using the config file
    openssl x509 -req -in "$CERTS_DIR/$name.csr" -CA "$CERTS_DIR/$CA_CERT" \
        -CAkey "$CERTS_DIR/$CA_KEY" -CAcreateserial -out "$CERTS_DIR/$name.cer" \
        -days "$VALIDITY_DAYS" -sha256 -extensions v3_req -extfile "$CERTS_DIR/${name}.conf" || {
        echo -e "${RED}Failed to sign CSR for $name${NC}"; exit 1;
    }

    # Import CA certificate into keystore
    keytool -import -trustcacerts -alias CARoot -file "$CERTS_DIR/$CA_CERT" \
        -keystore "$CERTS_DIR/$name.keystore.jks" -storepass "$PASSWORD" -noprompt || {
        echo -e "${RED}Failed to import CA cert into keystore for $name${NC}"; exit 1;
    }

    # Import signed certificate into keystore
    keytool -import -alias "$name" -file "$CERTS_DIR/$name.cer" \
        -keystore "$CERTS_DIR/$name.keystore.jks" -storepass "$PASSWORD" -noprompt || {
        echo -e "${RED}Failed to import signed cert into keystore for $name${NC}"; exit 1;
    }

    # Create truststore and import CA certificate
    keytool -import -trustcacerts -alias CARoot -file "$CERTS_DIR/$CA_CERT" \
        -keystore "$CERTS_DIR/$name.truststore.jks" -storepass "$PASSWORD" -noprompt || {
        echo -e "${RED}Failed to create truststore for $name${NC}"; exit 1;
    }

    echo -e "${GREEN}✓ Certificate for $name generated successfully${NC}"
}

# Generate certificates with proper SANs
generate_cert "zookeeper" "zookeeper" "DNS:zookeeper,DNS:localhost,IP:127.0.0.1"
generate_cert "kafka" "kafka" "DNS:kafka,DNS:localhost,IP:127.0.0.1"
generate_cert "client" "client" "DNS:client,DNS:localhost,IP:127.0.0.1"

# Verify truststores exist
echo -e "${YELLOW}Verifying generated files...${NC}"
for name in zookeeper kafka client; do
    if [ ! -f "$CERTS_DIR/$name.truststore.jks" ]; then
        echo -e "${RED}Error: Truststore $name.truststore.jks was not created.${NC}"
        exit 1
    fi
    if [ ! -f "$CERTS_DIR/$name.keystore.jks" ]; then
        echo -e "${RED}Error: Keystore $name.keystore.jks was not created.${NC}"
        exit 1
    fi
done

# Clean up temporary files
echo -e "${YELLOW}Cleaning up temporary files...${NC}"
rm -f "$CERTS_DIR"/*.csr "$CERTS_DIR"/*.cer "$CERTS_DIR"/*.conf "$CERTS_DIR"/*.srl

echo -e "${GREEN}🎉 Certificates and truststores generated successfully!${NC}"
echo "Generated files in $CERTS_DIR:"
ls -la "$CERTS_DIR"/*.jks
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Run: docker-compose -f docker-compose-ssl-fixed.yml up -d"
echo "2. Test SSL connection with: ./test-ssl-connection.sh"
echo "3. Access Kafka UI at: http://localhost:8080"
echo ""
echo -e "${GREEN}Password for all keystores and truststores: $PASSWORD${NC}"