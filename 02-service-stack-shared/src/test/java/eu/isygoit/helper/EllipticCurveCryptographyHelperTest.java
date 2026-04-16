package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EllipticCurveCryptographyHelper Tests")
class EllipticCurveCryptographyHelperTest {

    @Test
    @DisplayName("should generate valid key pair")
    void generateKeyPair_shouldReturnValidKeyPair() {
        Optional<KeyPair> keyPairOpt = EllipticCurveCryptographyHelper.generateKeyPair();
        assertTrue(keyPairOpt.isPresent());
        assertNotNull(keyPairOpt.get().getPrivate());
        assertNotNull(keyPairOpt.get().getPublic());
        assertEquals("EC", keyPairOpt.get().getPrivate().getAlgorithm());
    }

    @Test
    @DisplayName("should sign and verify data")
    void signAndVerify_shouldBeSuccessful() {
        KeyPair keyPair = EllipticCurveCryptographyHelper.createKeyPair();
        byte[] data = "test message".getBytes();

        Optional<byte[]> signatureOpt = EllipticCurveCryptographyHelper.signData(data, keyPair.getPrivate());
        assertTrue(signatureOpt.isPresent());
        byte[] signature = signatureOpt.get();

        boolean verified = EllipticCurveCryptographyHelper.verifySignature(data, signature, keyPair.getPublic());
        assertTrue(verified);

        boolean tampered = EllipticCurveCryptographyHelper.verifySignature("tampered".getBytes(), signature, keyPair.getPublic());
        assertFalse(tampered);
    }

    @Test
    @DisplayName("should convert to and from Base64")
    void base64Conversion_shouldBeCorrect() {
        byte[] data = "test content".getBytes();
        String base64 = EllipticCurveCryptographyHelper.toBase64(data);
        assertFalse(base64.isEmpty());

        byte[] decoded = EllipticCurveCryptographyHelper.fromBase64(base64);
        assertArrayEquals(data, decoded);
    }

    @Test
    @DisplayName("should convert keys to Base64")
    void keyToBase64_shouldBeCorrect() {
        KeyPair keyPair = EllipticCurveCryptographyHelper.createKeyPair();
        String privBase64 = EllipticCurveCryptographyHelper.privateKeyToBase64(keyPair.getPrivate());
        String pubBase64 = EllipticCurveCryptographyHelper.publicKeyToBase64(keyPair.getPublic());

        assertFalse(privBase64.isEmpty());
        assertFalse(pubBase64.isEmpty());
    }

    @Test
    @DisplayName("should handle nulls in conversions")
    void handleNulls_shouldReturnEmpty() {
        assertEquals("", EllipticCurveCryptographyHelper.toBase64(null));
        assertArrayEquals(new byte[0], EllipticCurveCryptographyHelper.fromBase64(null));
        assertEquals("", EllipticCurveCryptographyHelper.privateKeyToBase64(null));
        assertEquals("", EllipticCurveCryptographyHelper.publicKeyToBase64(null));
    }
}
