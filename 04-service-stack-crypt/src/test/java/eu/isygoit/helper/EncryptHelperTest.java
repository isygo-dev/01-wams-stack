package eu.isygoit.helper;

import eu.isygoit.data.DecryptData;
import eu.isygoit.data.EncryptData;
import eu.isygoit.data.KeyData;
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
     * Test create helper data.
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