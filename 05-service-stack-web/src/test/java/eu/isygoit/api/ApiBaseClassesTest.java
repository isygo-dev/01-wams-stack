package eu.isygoit.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbstractRequest and AbstractResponse Tests")
class ApiBaseClassesTest {

    @Test
    @DisplayName("AbstractRequest should be buildable using SuperBuilder")
    void testAbstractRequestSuperBuilder() {
        AbstractRequest request = AbstractRequest.builder().build();
        assertNotNull(request);
    }

    @Test
    @DisplayName("AbstractResponse should have error details and be buildable")
    void testAbstractResponse() {
        AbstractResponse response = AbstractResponse.builder()
                .hasError(true)
                .errorCode(404)
                .errorMessage("Not Found")
                .build();

        assertTrue(response.getHasError());
        assertEquals(404, response.getErrorCode());
        assertEquals("Not Found", response.getErrorMessage());
    }

    @Test
    @DisplayName("AbstractResponse should work with AllArgsConstructor")
    void testAbstractResponseAllArgsConstructor() {
        AbstractResponse response = new AbstractResponse(false, 0, "No Error");
        assertFalse(response.getHasError());
        assertEquals(0, response.getErrorCode());
        assertEquals("No Error", response.getErrorMessage());
    }
}
