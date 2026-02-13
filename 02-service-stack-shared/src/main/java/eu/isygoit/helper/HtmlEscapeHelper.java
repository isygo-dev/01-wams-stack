package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The type HtmlEscapeHelper.
 * A utility class for escaping special HTML characters in strings.
 */
public interface HtmlEscapeHelper {

    /**
     * The constant logger.
     */
    Logger logger = LoggerFactory.getLogger(HtmlEscapeHelper.class);

    /**
     * The constant HTML_CHAR_TO_ENTITY_MAP.
     */
// Predefined HTML character encoding table mapping characters to their HTML entities
    Map<Character, String> HTML_CHAR_TO_ENTITY_MAP = new HashMap<>() {{
        // Basic HTML escape codes
        put('<', "&lt;");  // Less-than sign
        put('>', "&gt;");  // Greater-than sign
        put('&', "&amp;"); // Ampersand
        put('\"', "&quot;"); // Double quote
        put('\'', "&#39;"); // Single quote (apostrophe)

        // Extended characters
        put('©', "&copy;"); // Copyright symbol
        put('®', "&reg;");  // Registered symbol
        put('€', "&euro;"); // Euro symbol
        put('£', "&pound;"); // Pound symbol
        put('¥', "&yen;"); // Yen symbol
        put('°', "&deg;"); // Degree symbol

        // Mathematical symbols
        put('±', "&plusmn;"); // Plus-minus sign
        put('×', "&times;"); // Multiplication sign
        put('÷', "&divide;"); // Division sign

        // Punctuation symbols
        put('•', "&bull;");  // Bullet symbol
        put('–', "&ndash;"); // En dash
        put('—', "&mdash;"); // Em dash
        put('‘', "&lsquo;"); // Left single quotation mark
        put('’', "&rsquo;"); // Right single quotation mark
        put('“', "&ldquo;"); // Left double quotation mark
        put('”', "&rdquo;"); // Right double quotation mark

        // Arrows
        put('←', "&larr;"); // Left arrow
        put('→', "&rarr;"); // Right arrow
        put('↑', "&uarr;"); // Up arrow
        put('↓', "&darr;"); // Down arrow
        put('↔', "&harr;"); // Left-right arrow

        // Brackets and braces
        put('(', "&lpar;"); // Left parenthesis
        put(')', "&rpar;"); // Right parenthesis
        put('[', "&lsqb;"); // Left square bracket
        put(']', "&rsqb;"); // Right square bracket
        put('{', "&lcub;"); // Left curly brace
        put('}', "&rcub;"); // Right curly brace

        // Other special characters
        put('™', "&trade;"); // Trademark symbol
        put('§', "&sect;"); // Section symbol
        put('¶', "&para;"); // Paragraph symbol
        put('∞', "&infin;"); // Infinity symbol
        put('√', "&radic;"); // Square root symbol
        put('∑', "&sum;"); // Summation symbol

        // Diacritical marks (accents and marks used in letters)
        put('á', "&aacute;"); // Small letter a with acute
        put('é', "&eacute;"); // Small letter e with acute
        put('í', "&iacute;"); // Small letter i with acute
        put('ó', "&oacute;"); // Small letter o with acute
        put('ú', "&uacute;"); // Small letter u with acute
        put('ñ', "&ntilde;"); // Small letter n with tilde
        put('ç', "&ccedil;"); // Small letter c with cedilla
        put('ø', "&oslash;"); // Small letter o with stroke

        // Accented uppercase letters
        put('Á', "&Aacute;"); // Uppercase letter A with acute
        put('É', "&Eacute;"); // Uppercase letter E with acute
        put('Í', "&Iacute;"); // Uppercase letter I with acute
        put('Ó', "&Oacute;"); // Uppercase letter O with acute
        put('Ú', "&Uacute;"); // Uppercase letter U with acute

        // Combining characters
        put('̆', "&breve;"); // Combining breve
        put('̃', "&tilde;"); // Combining tilde
        put('́', "&acute;"); // Combining acute accent
    }};

    /**
     * Escapes HTML special characters in a string based on the provided character-to-entity map.
     * The characters found in the input string will be replaced by their corresponding HTML entities
     * from the encoding map.
     *
     * @param inputString The input string to encode.
     * @param encodingMap The map that associates characters with their HTML entities.
     * @return The encoded string with HTML entities.
     */
    public static String escapeHtmlSpecialChars(String inputString, Map<Character, String> encodingMap) {
        if (inputString == null || encodingMap == null || encodingMap.isEmpty()) {
            logger.warn("The input string or encoding map is null or empty. Returning the original string.");
            return inputString; // Return original string if input is invalid.
        }

        // StringBuilder is used to accumulate the result for better performance
        StringBuilder encodedResult = new StringBuilder(inputString.length());

        // Process each character in the input string
        inputString.chars()
                .mapToObj(c -> (char) c)  // Convert int to Character
                .forEach(ch -> {
                    // Replace the character with its HTML entity if it exists in the encoding map
                    Optional.ofNullable(encodingMap.get(ch))
                            .ifPresentOrElse(
                                    encodedEntity -> encodedResult.append(encodedEntity),  // Append encoded entity
                                    () -> encodedResult.append(ch)  // Append the original character if no encoding found
                            );
                });

        // Return the encoded string, or the original string if no changes were made
        String finalResult = encodedResult.toString();
        if (!finalResult.equals(inputString)) {
            logger.debug("HTML-encoded string: {}", finalResult);
        }
        return finalResult.equals(inputString) ? inputString : finalResult;
    }

    /**
     * Encodes special HTML characters in the input string using a predefined encoding map.
     *
     * @param inputString The input string to encode.
     * @return The HTML-encoded string or the original string if no encoding is necessary.
     */
    public static String encodeHtml(String inputString) {
        logger.info("Encoding HTML for the input string.");
        return escapeHtmlSpecialChars(inputString, HTML_CHAR_TO_ENTITY_MAP);
    }
}