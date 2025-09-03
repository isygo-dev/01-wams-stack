package eu.isygoit.helper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for URL manipulations, extracting client information, and other helper methods.
 * Provides various helper methods to handle URLs, query parameters, headers, and HTTP request manipulations.
 */
public interface UrlHelper {

    /**
     * The constant logger.
     */
    Logger logger = LoggerFactory.getLogger(UrlHelper.class);

    /**
     * The constant SPECIAL_CHARACTERS.
     */
    public static final String[][] SPECIAL_CHARACTERS = {
            {" ", "#", "$", "%", "&", "@", "`", "/", ":", ";", "<", "=", ">", "?", "[", "\\", "]", "^", "{", "|", "}", "~", "“", "‘", "+", ","},
            {"20%", "24%", "26%", "60%", "%3A", "%3C", "%3E", "%5B", "%5D", "%7B", "%7D", "22%", "%2B", "23%", "25%", "40%", "%2F", "%3B", "%3D", "%3F", "%5C", "%5E", "%7C", "%7E", "27%", "%2C"}
    };

    /**
     * Escapes special characters in the input text by replacing them with their URL encoded equivalents.
     * This method helps prepare URLs that are safe to use in HTTP requests by encoding characters that may cause issues.
     *
     * @param inputText The input text to escape.
     * @return The input text with special characters replaced by URL encoded equivalents.
     */
    public static String escapeSpecialCharacters(String inputText) {
        String result = inputText;
        for (int i = 0; i < SPECIAL_CHARACTERS[0].length; i++) {
            result = result.replace(SPECIAL_CHARACTERS[0][i], SPECIAL_CHARACTERS[1][i]);
        }
        logger.debug("Escaped special characters in input text: {}", result);
        return result;
    }

    /**
     * URL encodes the provided text using UTF-8 encoding.
     * This ensures that the text is encoded correctly for use in a URL.
     *
     * @param text The text to encode.
     * @return The URL encoded string.
     * @throws UnsupportedEncodingException If encoding fails (shouldn't happen with UTF-8).
     */
    public static String encodeUrl(String text) throws UnsupportedEncodingException {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.name());
        logger.debug("Encoded text: {}", encodedText);
        return encodedText;
    }

    /**
     * URL decodes the provided text that was previously encoded.
     * This method reverses the URL encoding.
     *
     * @param text The text to decode.
     * @return The decoded string.
     * @throws UnsupportedEncodingException If decoding fails (shouldn't happen with UTF-8).
     */
    public static String decodeUrl(String text) throws UnsupportedEncodingException {
        String decodedText = URLDecoder.decode(text, StandardCharsets.UTF_8.name());
        logger.debug("Decoded text: {}", decodedText);
        return decodedText;
    }

    /**
     * Extracts the value of a specific query parameter from the given URL.
     * It returns the value of the parameter or an empty string if the parameter is not found.
     *
     * @param url       The URL to extract the parameter from.
     * @param paramName The name of the query parameter to extract.
     * @return The value of the parameter, or an empty string if not found.
     */
    public static String getQueryParameterValue(String url, String paramName) {
        String patternString = "([?&])" + paramName + "=([^&]*)";
        Pattern pattern = Pattern.compile(patternString);
        var matcher = pattern.matcher(url);
        String paramValue = matcher.find() ? matcher.group(2) : "";
        logger.debug("Extracted query parameter '{}' with value: {}", paramName, paramValue);
        return paramValue;
    }

    /**
     * Adds or updates a query parameter in the provided URL.
     * This method either appends a new query parameter or updates an existing one in the URL.
     *
     * @param url        The original URL.
     * @param paramName  The name of the query parameter to add or update.
     * @param paramValue The value of the parameter.
     * @return The updated URL with the new or updated query parameter.
     */
    public static String updateQueryParameter(String url, String paramName, String paramValue) {
        if (url == null || paramName == null || paramValue == null) {
            logger.warn("URL, parameter name, or parameter value is null. Returning original URL.");
            return url;
        }

        String baseUrl = url.split("\\?")[0];  // Extract base URL (before '?')
        String queryString = extractQueryString(url);  // Extract the existing query string, if any.

        String updatedQueryString = updateQueryString(queryString, paramName, paramValue);

        if (updatedQueryString.isEmpty()) {
            logger.debug("No query parameters found. Returning base URL.");
            return baseUrl;  // Return base URL if no query parameters.
        } else {
            String updatedUrl = baseUrl + "?" + updatedQueryString;
            logger.debug("Updated URL with new query parameter: {}", updatedUrl);
            return updatedUrl;
        }
    }

    /**
     * Helper method to handle adding or updating a query parameter in an existing query string.
     *
     * @param queryString The current query string (e.g., "param1=value1&param2=value2").
     * @param paramName   The name of the query parameter to add or update.
     * @param paramValue  The value of the parameter.
     * @return The updated query string.
     */
    public static String updateQueryString(String queryString, String paramName, String paramValue) {
        if (queryString.isEmpty()) {
            return paramName + "=" + paramValue;  // No existing parameters, just add the new one.
        }

        // Parse the query string into a map of parameters.
        var params = Arrays.stream(queryString.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(param -> param[0], param -> param.length > 1 ? param[1] : ""));

        // Add or update the parameter.
        params.put(paramName, paramValue);

        // Rebuild the query string with updated parameters.
        String updatedQueryString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        logger.debug("Updated query string: {}", updatedQueryString);
        return updatedQueryString;
    }

    /**
     * Extracts the query string (everything after the '?' character) from the URL.
     * If no query string exists, it returns an empty string.
     *
     * @param url The URL to extract the query string from.
     * @return The query string, or an empty string if no query string exists.
     */
    public static String extractQueryString(String url) {
        String[] parts = url.split("\\?");
        return parts.length > 1 ? parts[1] : "";
    }

    /**
     * Retrieves the client's IP address from the HTTP request.
     * This method checks common headers like X-FORWARDED-FOR and falls back to the remote address if not available.
     *
     * @param request The HTTP request.
     * @return The client's IP address.
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            logger.warn("Request is null, cannot retrieve IP address.");
            return "";
        }

        String ipAddress = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR"))
                .filter(StringUtils::hasText)
                .orElseGet(() -> request.getRemoteAddr());

        logger.debug("Client IP address: {}", ipAddress);
        return ipAddress;
    }

    /**
     * Identifies the type of device making the request based on the User-Agent header.
     * The method returns "Mobile", "iPad", or "Desktop" based on the user agent.
     *
     * @param request The HTTP request.
     * @return The type of device ("Mobile", "iPad", or "Desktop").
     */
    public static String getDeviceType(HttpServletRequest request) {
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");
        String deviceType = userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone") ?
                "Mobile" : userAgent.contains("iPad") ? "iPad" : "Desktop";

        logger.debug("Device type based on User-Agent: {}", deviceType);
        return deviceType;
    }

    /**
     * Identifies the browser used by the client based on the User-Agent string.
     * This method checks for common browsers like Chrome, Firefox, Edge, and Opera.
     *
     * @param request The HTTP request.
     * @return The browser name (e.g., "Chrome", "Firefox", etc.).
     */
    public static String getBrowserType(HttpServletRequest request) {
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");
        String browser = Arrays.stream(new String[]{"Chrome", "Firefox", "Edg", "Opera", "OPR"})
                .filter(userAgent::contains)
                .findFirst()
                .map(browserType -> switch (browserType) {
                    case "Chrome" -> "Google Chrome";
                    case "Firefox" -> "Firefox";
                    case "Edg" -> "Microsoft Edge";
                    case "Opera", "OPR" -> "Opera";
                    default -> "Other Browser";
                })
                .orElse("Other Browser");

        logger.debug("Browser type identified from User-Agent: {}", browser);
        return browser;
    }

    /**
     * Extracts the JWT (JSON Web Token) from the Authorization header of the HTTP request.
     * If the token is found, it returns the token, otherwise null.
     *
     * @param request The HTTP request.
     * @return The JWT token, or null if not found or improperly formatted.
     */
    public static String getJwtTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = Optional.ofNullable(request.getHeader("Authorization")).orElse("");

        if (authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            logger.debug("JWT token extracted: {}", jwtToken);
            return jwtToken;
        }

        logger.warn("JWT token not found or improperly formatted.");
        return null;
    }

    /**
     * Checks if the provided string value is either null or contains the text "null".
     * This method is useful for detecting invalid or uninitialized parameters.
     *
     * @param value The value to check.
     * @return True if the value is null or contains "null", otherwise false.
     */
    public static boolean isNullOrEmpty(String value) {
        boolean isNull = !StringUtils.hasText(value) || "null" .equals(value);
        if (isNull) {
            logger.warn("Parameter is either null or contains the string 'null'.");
        }
        return isNull;
    }

    /**
     * Retrieves a specific cookie from the HTTP request by its name.
     * If the cookie is found, its value is returned; otherwise, null.
     *
     * @param request    The HTTP request.
     * @param cookieName The name of the cookie to retrieve.
     * @return The cookie value, or null if not found.
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    logger.debug("Cookie '{}' found with value: {}", cookieName, cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        logger.warn("Cookie '{}' not found in the request.", cookieName);
        return null;
    }

    /**
     * Retrieves the session ID of the HTTP request.
     * If a session exists, the session ID is returned; otherwise, null is returned.
     *
     * @param request The HTTP request.
     * @return The session ID, or null if no session exists.
     */
    public static String getSessionId(HttpServletRequest request) {
        String sessionId = request.getSession(false) != null ? request.getSession().getId() : null;
        logger.debug("Session ID: {}", sessionId);
        return sessionId;
    }

    /**
     * Retrieves the content type of the HTTP request (e.g., "application/json").
     * This method can be used to check if the request body is in a specific format.
     *
     * @param request The HTTP request.
     * @return The content type of the request.
     */
    public static String getContentType(HttpServletRequest request) {
        String contentType = Optional.ofNullable(request.getContentType()).orElse("unknown");
        logger.debug("Content type of the request: {}", contentType);
        return contentType;
    }

    /**
     * Checks if the content type of the HTTP request is "application/json".
     * This method is useful for processing JSON data in the request body.
     *
     * @param request The HTTP request.
     * @return True if the content type is JSON, otherwise false.
     */
    public static boolean isJsonContent(HttpServletRequest request) {
        boolean isJson = "application/json" .equalsIgnoreCase(request.getContentType());
        logger.debug("Is content type JSON: {}", isJson);
        return isJson;
    }

    /**
     * Checks if the content type of the HTTP request is "application/xml" or "text/xml".
     * This method is useful for processing XML data in the request body.
     *
     * @param request The HTTP request.
     * @return True if the content type is XML, otherwise false.
     */
    public static boolean isXmlContent(HttpServletRequest request) {
        boolean isXml = "application/xml" .equalsIgnoreCase(request.getContentType()) ||
                "text/xml" .equalsIgnoreCase(request.getContentType());
        logger.debug("Is content type XML: {}", isXml);
        return isXml;
    }

    /**
     * Retrieves the body content of the HTTP request as a string.
     * This method reads the input stream of the request to extract its body.
     *
     * @param request The HTTP request.
     * @return The body content as a string.
     * @throws IOException If an error occurs reading the request body.
     */
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        logger.debug("Request body: {}", body);
        return body.toString();
    }

    /**
     * Retrieves the HTTP method used for the request (e.g., GET, POST, etc.).
     * This method can be useful for identifying the type of HTTP request.
     *
     * @param request The HTTP request.
     * @return The HTTP method (e.g., GET, POST).
     */
    public static String getRequestMethod(HttpServletRequest request) {
        String method = request.getMethod();
        logger.debug("HTTP request method: {}", method);
        return method;
    }

    /**
     * Retrieves the value of a specific header from the HTTP request.
     *
     * @param request    The HTTP request.
     * @param headerName The name of the header.
     * @return The header value, or null if the header is not found.
     */
    public static String getHeaderValue(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        logger.debug("Header '{}' value: {}", headerName, headerValue);
        return headerValue;
    }

    /**
     * Checks if the User-Agent indicates that the request is from a mobile device.
     *
     * @param request The HTTP request.
     * @return True if the request comes from a mobile device, otherwise false.
     */
    public static boolean isMobileUserAgent(HttpServletRequest request) {
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");
        boolean isMobile = Pattern.compile(".*(Mobile|Android|iPhone|iPad).*").matcher(userAgent).matches();
        logger.debug("Is mobile user agent: {}", isMobile);
        return isMobile;
    }
}