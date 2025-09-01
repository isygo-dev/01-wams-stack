#!/bin/bash
# Ensure a clean state
rm -rf kafka-config/secrets
mkdir -p kafka-config/secrets
cd kafka-config/secrets

# Generate CA and certificates, escaping subject for MINGW64
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -subj "//CN=localhost" -nodes || {
  echo "Error: openssl req failed. Ensure openssl is installed and try running in a Windows-native shell (CMD/PowerShell)."
  exit 1
}

# Use the same password for storepass and keypass to avoid PKCS12 warning
keytool -keystore kafka.keystore.jks -alias localhost -validity 365 -genkey -keyalg RSA \
  -storepass keystore-password -keypass keystore-password -dname "CN=localhost" -noprompt || {
  echo "Error: keytool -genkey failed. Check if keytool is installed or if kafka.keystore.jks is corrupted."
  exit 1
}

keytool -keystore kafka.keystore.jks -alias CARoot -import -file ca-cert \
  -storepass keystore-password -noprompt || {
  echo "Error: keytool -import for kafka.keystore.jks failed."
  exit 1
}

keytool -keystore kafka.truststore.jks -alias CARoot -import -file ca-cert \
  -storepass truststore-password -noprompt || {
  echo "Error: keytool -import for kafka.truststore.jks failed."
  exit 1
}

echo "keystore-password" > keystore_creds
echo "keystore-password" > key_creds
echo "truststore-password" > truststore_creds

# Set restrictive permissions
chmod 600 ca-key ca-cert kafka.keystore.jks kafka.truststore.jks keystore_creds key_creds truststore_creds
chmod 700 .