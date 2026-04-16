package eu.isygoit.config;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import eu.isygoit.enums.IEnumJwtStorage;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtProperties Tests")
class JwtPropertiesTest {

    @Test
    @DisplayName("should correctly hold JWT properties")
    void testJwtProperties() {
        JwtProperties properties = new JwtProperties();
        
        ReflectionTestUtils.setField(properties, "secretKey", "testSecret");
        ReflectionTestUtils.setField(properties, "signatureAlgorithm", SignatureAlgorithm.HS256);
        ReflectionTestUtils.setField(properties, "lifeTimeInMs", 3600000);
        ReflectionTestUtils.setField(properties, "jwtStorageType", IEnumJwtStorage.Types.LOCAL);

        assertEquals("testSecret", properties.getSecretKey());
        assertEquals(SignatureAlgorithm.HS256, properties.getSignatureAlgorithm());
        assertEquals(3600000, properties.getLifeTimeInMs());
        assertEquals(IEnumJwtStorage.Types.LOCAL, properties.getJwtStorageType());
    }
}
