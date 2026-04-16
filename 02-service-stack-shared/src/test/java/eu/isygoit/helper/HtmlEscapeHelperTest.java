package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("HtmlEscapeHelper Tests")
class HtmlEscapeHelperTest {

    @Test
    @DisplayName("should encode basic HTML characters")
    void encodeHtml_shouldEncodeBasicChars() {
        String input = "Hello <world> & \"friend\" '";
        String expected = "Hello &lt;world&gt; &amp; &quot;friend&quot; &#39;";
        assertEquals(expected, HtmlEscapeHelper.encodeHtml(input));
    }

    @Test
    @DisplayName("should encode extended characters")
    void encodeHtml_shouldEncodeExtendedChars() {
        String input = "© ® €";
        String expected = "&copy; &reg; &euro;";
        assertEquals(expected, HtmlEscapeHelper.encodeHtml(input));
    }

    @Test
    @DisplayName("should return original string if no encoding needed")
    void encodeHtml_shouldReturnOriginalIfNoEncoding() {
        String input = "Hello World";
        assertEquals(input, HtmlEscapeHelper.encodeHtml(input));
    }

    @Test
    @DisplayName("should handle null input")
    void encodeHtml_shouldHandleNull() {
        assertNull(HtmlEscapeHelper.encodeHtml(null));
    }

    @Test
    @DisplayName("should use custom encoding map")
    void escapeHtmlSpecialChars_shouldUseCustomMap() {
        Map<Character, String> customMap = Map.of('A', "1", 'B', "2");
        String input = "ABC";
        String expected = "12C";
        assertEquals(expected, HtmlEscapeHelper.escapeHtmlSpecialChars(input, customMap));
    }
}
