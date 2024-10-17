package eu.isygoit.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * The type Security helper.
 */
@Slf4j
@Component
public class SecurityHelper {

    private static final String AES_ALGORITHM = "AES";

    private static final String AES_WRAP_TRANSFORMATION = "AESWrap";

    /**
     * Generate ec private key ec private key.
     *
     * @param hexString the hex string
     * @return the ec private key
     */
    public static ECPrivateKey generateECPrivateKey(String hexString) {
        ECPrivateKey privateKey = null;
        try {
            byte[] encodedKey = ByteArrayHelper.hexStringToByteArray(hexString);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
            privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("<Error>: error accured : " + e.getMessage());
        }
        return privateKey;
    }

    /**
     * Generate ec public key ec public key.
     *
     * @param hexString the hex string
     * @return the ec public key
     */
    public static ECPublicKey generateEcPublicKey(String hexString) {
        byte[] encodedKey = ByteArrayHelper.hexStringToByteArray(hexString);
        ECPublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
            KeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
            publicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("<Error>: error accured : " + e.getMessage());
        }

        return publicKey;
    }

    /**
     * Generate random sequence byte [ ].
     *
     * @return the byte [ ]
     */
    public static byte[] generateRandomSequence() {
        SecureRandom rand = new SecureRandom();
        // Random challenge must have a range of 8 to 64 bytes
        // limit the challenge to 16 bytes -- //
        // int resultLength = rand.nextInt(57) + 8;
        int resultLength = 16;
        byte[] result = new byte[resultLength];

        for (int i = 0; i < resultLength; i++) {
            byte[] resultByte = new byte[1];

            // Only allow printable characters
            do {
                rand.nextBytes(resultByte);
            } while (resultByte[0] >= 0 && resultByte[0] <= 31);

            result[i] = resultByte[0];
        }

        return result;
    }

    /**
     * Wrap aes string.
     *
     * @param secretKey the secret key
     * @param data      the data
     * @return the string
     */
    public static String wrapAES(String secretKey, String data) {
        SecretKey key = new SecretKeySpec(ByteArrayHelper.hexStringToByteArray(secretKey), AES_ALGORITHM);

        Key keyDataKey = new SecretKeySpec(ByteArrayHelper.hexStringToByteArray(data), AES_ALGORITHM);
        // wrap key
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(AES_WRAP_TRANSFORMATION);
            cipher.init(Cipher.WRAP_MODE, key);
            byte[] wrappedKeyBytes;
            wrappedKeyBytes = cipher.wrap(keyDataKey);
            return ByteArrayHelper.byteArrayToHexString(wrappedKeyBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error("<Error>: NoSuchAlgorithmException | NoSuchPaddingException " + e);
        } catch (InvalidKeyException e) {
            log.error("<Error>: Error InvalidKeyException: " + e);
        } catch (IllegalBlockSizeException e) {
            log.error("<Error>: Error IllegalBlockSizeException: " + e);
        }
        return null;

    }

}
