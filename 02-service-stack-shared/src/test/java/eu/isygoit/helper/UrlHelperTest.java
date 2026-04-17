package eu.isygoit.helper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UrlHelper Tests")
class UrlHelperTest {

    @Test
    @DisplayName("isNullOrEmpty should work correctly")
    void testIsNullOrEmpty() {
        assertTrue(UrlHelper.isNullOrEmpty(null));
        assertTrue(UrlHelper.isNullOrEmpty(""));
        assertTrue(UrlHelper.isNullOrEmpty("  "));
        assertFalse(UrlHelper.isNullOrEmpty("abc"));
    }

    @Nested
    @DisplayName("Encoding/Decoding Tests")
    class EncodingTests {

        @Test
        @DisplayName("should escape special characters")
        void testEscapeSpecialCharacters() {
            String input = "hello world";
            String escaped = UrlHelper.escapeSpecialCharacters(input);
            // According to the table, ' ' is at index 0 and replaced by '20%' at index 0.
            // Wait, looking at the table:
            // {" ", "#", ...}
            // {"20%", "24%", ...}
            // So "hello world" should become "hello20%world"
            assertEquals("hello20%world", escaped);
        }

        @Test
        @DisplayName("should encode and decode URL")
        void testEncodeDecodeUrl() throws UnsupportedEncodingException {
            String original = "hello world!";
            String encoded = UrlHelper.encodeUrl(original);
            assertEquals("hello+world%21", encoded);
            assertEquals(original, UrlHelper.decodeUrl(encoded));
        }
    }

    @Nested
    @DisplayName("Query Parameter Tests")
    class QueryParamTests {

        @Test
        @DisplayName("should get query parameter value")
        void testGetQueryParameterValue() {
            String url = "http://example.com?param1=val1&param2=val2";
            assertEquals("val1", UrlHelper.getQueryParameterValue(url, "param1"));
            assertEquals("val2", UrlHelper.getQueryParameterValue(url, "param2"));
            assertEquals("", UrlHelper.getQueryParameterValue(url, "param3"));
        }

        @Test
        @DisplayName("should update query parameter")
        void testUpdateQueryParameter() {
            String url = "http://example.com?p1=v1";
            String updated = UrlHelper.updateQueryParameter(url, "p1", "v2");
            assertTrue(updated.contains("p1=v2"));

            String added = UrlHelper.updateQueryParameter(url, "p2", "v2");
            assertTrue(added.contains("p1=v1"));
            assertTrue(added.contains("p2=v2"));
        }

        @Test
        @DisplayName("should extract query string")
        void testExtractQueryString() {
            String url = "http://example.com?p1=v1&p2=v2";
            assertEquals("p1=v1&p2=v2", UrlHelper.extractQueryString(url));
        }
    }

    @Nested
    @DisplayName("HttpServletRequest Tests")
    class RequestTests {

        @Test
        @DisplayName("should get client IP address")
        void testGetClientIpAddress() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-FORWARDED-FOR")).thenReturn("1.2.3.4");
            assertEquals("1.2.3.4", UrlHelper.getClientIpAddress(request));

            when(request.getHeader("X-FORWARDED-FOR")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");
            assertEquals("127.0.0.1", UrlHelper.getClientIpAddress(request));
        }

        @Test
        @DisplayName("should get JWT token from request")
        void testGetJwtTokenFromRequest() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Bearer myToken");
            assertEquals("myToken", UrlHelper.getJwtTokenFromRequest(request));

            when(request.getHeader("Authorization")).thenReturn("InvalidToken");
            assertNull(UrlHelper.getJwtTokenFromRequest(request));
        }

        @Test
        @DisplayName("should get cookie value")
        void testGetCookieValue() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie cookie = new Cookie("myCookie", "myVal");
            when(request.getCookies()).thenReturn(new Cookie[]{cookie});
            assertEquals("myVal", UrlHelper.getCookieValue(request, "myCookie"));
            assertNull(UrlHelper.getCookieValue(request, "other"));
        }

        @Test
        @DisplayName("should identify JSON content")
        void testIsJsonContent() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getContentType()).thenReturn("application/json");
            assertTrue(UrlHelper.isJsonContent(request));

            when(request.getContentType()).thenReturn("text/plain");
            assertFalse(UrlHelper.isJsonContent(request));
        }

        @Test
        @DisplayName("should get session ID")
        void testGetSessionId() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpSession session = mock(HttpSession.class);
            when(session.getId()).thenReturn("session123");
            when(request.getSession(false)).thenReturn(session);
            assertEquals("session123", UrlHelper.getSessionId(request));

            when(request.getSession(false)).thenReturn(null);
            assertNull(UrlHelper.getSessionId(request));
        }
    }
}
