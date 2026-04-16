package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityHelper Tests")
class SecurityHelperTest {

    @Nested
    @DisplayName("Random Generation Tests")
    class RandomGenerationTests {

        @Test
        @DisplayName("should generate 16-byte random sequence")
        void testGenerateRandomByteSequence() {
            byte[] sequence = SecurityHelper.generateRandomByteSequence();
            assertNotNull(sequence);
            assertEquals(16, sequence.length);
            // Verify all characters are in printable range 32-126
            for (byte b : sequence) {
                assertTrue(b >= 32 && b <= 126, "Byte " + b + " is not in range [32, 126]");
            }
        }

        @Test
        @DisplayName("should generate cryptographic salt")
        void testGenerateCryptographicSalt() {
            int length = 24;
            byte[] salt = SecurityHelper.generateCryptographicSalt(length);
            assertEquals(length, salt.length);
        }
    }

    @Nested
    @DisplayName("Key Generation Tests")
    class KeyGenerationTests {

        @Test
        @DisplayName("should generate RSA key pair")
        void testGenerateKeyPairRSA() {
            KeyPair keyPair = SecurityHelper.generateKeyPairForAlgorithm("RSA", 2048);
            assertNotNull(keyPair);
            assertNotNull(keyPair.getPublic());
            assertNotNull(keyPair.getPrivate());
        }

        @Test
        @DisplayName("should generate EC key pair and round-trip from hex")
        void testECKeyRoundTrip() {
            KeyPair keyPair = SecurityHelper.generateKeyPairForAlgorithm("EC", 256);
            assertNotNull(keyPair);

            String privateHex = ByteArrayHelper.convertBytesToHex(keyPair.getPrivate().getEncoded());
            String publicHex = ByteArrayHelper.convertBytesToHex(keyPair.getPublic().getEncoded());

            ECPrivateKey privateKey = SecurityHelper.generateECPrivateKeyFromHex(privateHex);
            ECPublicKey publicKey = SecurityHelper.generateECPublicKeyFromHex(publicHex);

            assertNotNull(privateKey);
            assertNotNull(publicKey);
        }
    }

    @Nested
    @DisplayName("AES Operations Tests")
    class AesOperationsTests {

        private final String hexKey = "00112233445566778899AABBCCDDEEFF";

        @Test
        @DisplayName("should encrypt and decrypt with AES")
        void testEncryptDecryptAES() {
            String plaintext = "Hello Security World";
            String ciphertext = SecurityHelper.encryptWithAES(hexKey, plaintext);
            assertNotNull(ciphertext);
            assertNotEquals(plaintext, ciphertext);

            String decrypted = SecurityHelper.decryptWithAES(hexKey, ciphertext);
            assertEquals(plaintext, decrypted);
        }

        @Test
        @DisplayName("should wrap AES key")
        void testWrapAESKey() {
            String dataKey = "FFEEDDCCBBAA99887766554433221100";
            String wrapped = SecurityHelper.wrapAESKeyWithSecretKey(hexKey, dataKey);
            assertNotNull(wrapped);
            assertTrue(wrapped.length() > 0);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("should validate hex string")
        void testIsHexStringValid() {
            assertTrue(SecurityHelper.isHexStringValid("ABCDEF0123"));
            assertTrue(SecurityHelper.isHexStringValid("abcdef"));
            assertFalse(SecurityHelper.isHexStringValid("ABCDEFG")); // G is not hex
            assertFalse(SecurityHelper.isHexStringValid(null));
            assertFalse(SecurityHelper.isHexStringValid(""));
            assertFalse(SecurityHelper.isHexStringValid("123")); // Odd length
        }
    }
}
