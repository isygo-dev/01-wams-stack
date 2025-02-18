package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Optional;

/**
 * Utility helper for signing and verifying messages using Elliptic Curve cryptography (ECC).
 * <p>
 * This class provides methods for generating EC key pairs, signing data with a private key,
 * verifying signatures with a public key, and converting keys and signatures to Base64 format.
 */
public interface EllipticCurveCryptographyHelper {

    Logger logger = LoggerFactory.getLogger(EllipticCurveCryptographyHelper.class);

    public static final String EC_ALGORITHM = "EC";
    public static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    public static final String EC_CURVE = "secp256r1"; // Commonly used ECC curve for security and performance

    /**
     * Generates a new Elliptic Curve (EC) key pair using the secp256r1 curve.
     * Logs success or failure based on the result.
     *
     * @return An {@link Optional} containing the generated {@link KeyPair}, or an empty Optional if an error occurs.
     */
    public static Optional<KeyPair> generateKeyPair() {
        return Optional.ofNullable(createKeyPair());
    }

    public static KeyPair createKeyPair() {
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance(EC_ALGORITHM);
            keyPairGenerator.initialize(new ECGenParameterSpec(EC_CURVE), new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            logger.info("Successfully generated EC key pair.");
            return keyPair;
        } catch (GeneralSecurityException e) {
            logger.error("Failed to generate EC key pair: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Signs the given data using the provided private key.
     * Logs success or failure based on the result.
     *
     * @param data       The data to be signed.
     * @param privateKey The private key used for signing.
     * @return An {@link Optional} containing the signed data (byte array) if successful, or an empty Optional if an error occurs.
     */
    public static Optional<byte[]> signData(byte[] data, PrivateKey privateKey) {
        return Optional.ofNullable(generateSignature(data, privateKey));
    }

    public static byte[] generateSignature(byte[] data, PrivateKey privateKey) {
        try {
            var signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data);
            byte[] signedData = signature.sign();
            logger.info("Data successfully signed.");
            return signedData;
        } catch (GeneralSecurityException e) {
            logger.error("Error signing data: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Verifies the provided signature against the original message using the given public key.
     * Logs whether the verification is successful or not.
     *
     * @param message   The original message.
     * @param signature The signature to be verified.
     * @param publicKey The public key used for verification.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     */
    public static boolean verifySignature(byte[] message, byte[] signature, PublicKey publicKey) {
        try {
            var signatureInstance = Signature.getInstance(SIGNATURE_ALGORITHM);
            signatureInstance.initVerify(publicKey);
            signatureInstance.update(message);
            boolean isVerified = signatureInstance.verify(signature);
            logger.info("Signature verification {}", isVerified ? "successful" : "failed");
            return isVerified;
        } catch (GeneralSecurityException e) {
            logger.error("Error verifying signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Converts the given byte array to a Base64 encoded string.
     * Logs the success of the conversion.
     *
     * @param data The byte array to encode.
     * @return The Base64 encoded string.
     */
    public static String toBase64(byte[] data) {
        return Optional.ofNullable(data)
                .map(Base64.getEncoder()::encodeToString)
                .orElse("");
    }

    /**
     * Decodes a Base64 encoded string back into a byte array.
     * Logs the success of the conversion.
     *
     * @param base64 The Base64 encoded string.
     * @return The decoded byte array.
     */
    public static byte[] fromBase64(String base64) {
        return Optional.ofNullable(base64)
                .map(Base64.getDecoder()::decode)
                .orElse(new byte[0]);
    }

    /**
     * Converts a private key to a Base64 encoded string.
     * Logs the result of the conversion.
     *
     * @param privateKey The private key to convert.
     * @return A Base64 encoded string representation of the key.
     */
    public static String privateKeyToBase64(PrivateKey privateKey) {
        return Optional.ofNullable(privateKey)
                .map(PrivateKey::getEncoded)
                .map(EllipticCurveCryptographyHelper::toBase64)
                .orElse("");
    }

    /**
     * Converts a public key to a Base64 encoded string.
     * Logs the result of the conversion.
     *
     * @param publicKey The public key to convert.
     * @return A Base64 encoded string representation of the key.
     */
    public static String publicKeyToBase64(PublicKey publicKey) {
        return Optional.ofNullable(publicKey)
                .map(PublicKey::getEncoded)
                .map(EllipticCurveCryptographyHelper::toBase64)
                .orElse("");
    }

    /**
     * Converts a digital signature to a Base64 encoded string.
     * Logs the result of the conversion.
     *
     * @param signature The digital signature.
     * @return A Base64 encoded string representation of the signature.
     */
    public static String signatureToBase64(byte[] signature) {
        return toBase64(signature);
    }
}