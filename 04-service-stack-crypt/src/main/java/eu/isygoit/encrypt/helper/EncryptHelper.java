package eu.isygoit.encrypt.helper;

import eu.isygoit.encrypt.data.DecryptData;
import eu.isygoit.encrypt.data.EncryptData;
import eu.isygoit.encrypt.data.KeyData;

import java.util.Date;

/**
 * The type Encrypt helper.
 */
public class EncryptHelper {

    /**
     * Create decrypt data decrypt data.
     *
     * @param <V>   the type parameter
     * @param value the value
     * @return the decrypt data
     */
    public static <V> DecryptData<V> createDecryptData(V value) {
        DecryptData<V> decryptData = new DecryptData<>();
        decryptData.setCalculationDate(new Date());
        decryptData.setValue(value);
        decryptData.setDurationInMs((new Date()).getTime() - decryptData.getCalculationDate().getTime());
        return decryptData;
    }

    /**
     * Create encrypt data encrypt data.
     *
     * @param <V>   the type parameter
     * @param value the value
     * @return the encrypt data
     */
    public static <V> EncryptData<V> createEncryptData(V value) {
        EncryptData<V> encryptData = new EncryptData<>();
        encryptData.setCalculationDate(new Date());
        encryptData.setValue(value);
        encryptData.setDurationInMs((new Date()).getTime() - encryptData.getCalculationDate().getTime());
        return encryptData;
    }

    /**
     * Create key data key data.
     *
     * @param value the value
     * @return the key data
     */
    public static KeyData createKeyData(String value) {
        KeyData keyData = new KeyData();
        keyData.setCalculationDate(new Date());
        keyData.setValue(value);
        keyData.setDurationInMs((new Date()).getTime() - keyData.getCalculationDate().getTime());
        return keyData;
    }
}
