package eu.isygoit.helper;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;

/**
 * The type Elliptic curve processor helper.
 */
public interface EllipticCurveProcessorHelper {

    Logger logger = LoggerFactory.getLogger(EllipticCurveProcessorHelper.class);
    
    /**
     * Sign data byte [ ].
     *
     * @param data       the data
     * @param privateKey the private key
     * @return the byte [ ]
     */
    public static byte[] signData(byte[] data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey, new SecureRandom());
            signature.update(data);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            logger.error("<Error>: an Error occured when signing data", e);
            return null;
        }
    }

    /**
     * Verify signature boolean.
     *
     * @param message        the message
     * @param tokenSignature the token signature
     * @param publicKey      the public key
     * @return the boolean
     */
    public static boolean verifySignature(byte[] message, byte[] tokenSignature, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(message);
            return signature.verify(tokenSignature);
        } catch (GeneralSecurityException e) {
            logger.error("<Error>: Signature verification failed", e);
            return false;
        }
    }

}
