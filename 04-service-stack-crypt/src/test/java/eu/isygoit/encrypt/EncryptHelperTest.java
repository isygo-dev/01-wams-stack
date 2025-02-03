package eu.isygoit.encrypt;

import eu.isygoit.encrypt.data.DecryptData;
import eu.isygoit.encrypt.data.EncryptData;
import eu.isygoit.encrypt.data.KeyData;
import eu.isygoit.encrypt.helper.EncryptHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * The type Encrypt helper test.
 */
class EncryptHelperTest {

    @Mock
    private Date mockDate;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test create decrypt data.
     */
    @Test
    void testCreateDecryptData() {
        // Arrange
        String value = "testValue";
        long currentTime = 1000L;
        when(mockDate.getTime()).thenReturn(currentTime);

        // Act
        DecryptData<String> decryptData = EncryptHelper.createDecryptData(value);

        // Assert
        assertNotNull(decryptData);
        assertEquals(value, decryptData.getValue());
    }

    /**
     * Test create encrypt data.
     */
    @Test
    void testCreateEncryptData() {
        // Arrange
        String value = "testValue";
        long currentTime = 2000L;
        when(mockDate.getTime()).thenReturn(currentTime);

        // Act
        EncryptData<String> encryptData = EncryptHelper.createEncryptData(value);

        // Assert
        assertNotNull(encryptData);
        assertEquals(value, encryptData.getValue());
    }

    /**
     * Test create key data.
     */
    @Test
    void testCreateKeyData() {
        // Arrange
        String value = "testKey";
        long currentTime = 3000L;
        when(mockDate.getTime()).thenReturn(currentTime);

        // Act
        KeyData keyData = EncryptHelper.createKeyData(value);

        // Assert
        assertNotNull(keyData);
        assertEquals(value, keyData.getValue());
    }
}
