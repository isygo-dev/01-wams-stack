package eu.isygoit.converter;

import jakarta.persistence.AttributeConverter;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Base64;


/**
 * The type Encryptor converter.
 */
//@Component
public class EncryptorConverter implements AttributeConverter<String, String> {

    /*
     Can be used on entity column :
     //@Convert(converter = EncryptorHelper.class)
     */
    public static final String AES = "AES";
    public static final String SECRET = "secret-key-12345";

    private final Key key;
    private final Cipher cipher;

    /**
     * Instantiates a new Encryptor converter.
     *
     * @throws Exception the exception
     */
    public EncryptorConverter() throws Exception {
        key = new SecretKeySpec(SECRET.getBytes(), AES);
        cipher = Cipher.getInstance(AES);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new IllegalStateException(e);
        }
    }
}
