package eu.isygoit.helper;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for HMAC (Hash-based Message Authentication Code) operations.
 */
@Slf4j
public class HmacHelper {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generates an HMAC-SHA256 signature for the given message and secret.
     *
     * @param message the message to sign
     * @param secret  the secret key
     * @return the Base64 encoded signature, or null if generation fails
     */
    public static String generateHmac(String message, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating HMAC signature", e);
            return null;
        }
    }

    /**
     * Validates an HMAC-SHA256 signature.
     *
     * @param message   the original message
     * @param signature the signature to validate
     * @param secret    the secret key
     * @return true if the signature is valid, false otherwise
     */
    public static boolean validateHmac(String message, String signature, String secret) {
        if (message == null || signature == null || secret == null) {
            return false;
        }
        String generated = generateHmac(message, secret);
        return signature.equals(generated);
    }
}
