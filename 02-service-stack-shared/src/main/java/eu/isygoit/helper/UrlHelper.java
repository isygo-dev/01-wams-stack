package eu.isygoit.helper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * The type Url helper.
 */
@Slf4j
public class UrlHelper {
    /**
     * The constant specialCharac.
     */
    /*
     * Table of URL Escape Characters Space $ & ` : < > [ ] { } “ + # % @ / ; = ? \ ^ | ~ ‘ , 20% 24% 26% 60% %3A %3C %3E %5B %5D %7B %7D 22% %2B 23% 25% 40% %2F %3B %3D %3F %5C %5E %7C %7E 27% %2C
     */
    public static final String[][] specialCharac = {{" ", "#", "$", "%", "&", "@", "`", "/", ":", ";", "<", "=", ">", "?", "[", "\\", "]", "^", "{", "|", "}", "~", "“", "‘", "+", ","},
            {"20%", "24%", "26%", "60%", "%3A", "%3C", "%3E", "%5B", "%5D", "%7B", "%7D", "22%", "%2B", "23%", "25%", "40%", "%2F", "%3B", "%3D", "%3F", "%5C", "%5E", "%7C", "%7E", "27%", "%2C"}};

    /**
     * Escape characters string.
     *
     * @param text the text
     * @return the string
     */
    public static String escapeCharacters(String text) {
        for (int i = 0; i < specialCharac.length; i++) {
            text = text.replace(specialCharac[0][i], specialCharac[1][i]);
        }
        return text;
    }

    /**
     * Gets client ip.
     *
     * @param request the request
     * @return the client ip
     */
    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (!StringUtils.hasText(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    /**
     * Gets device.
     *
     * @param request the request
     * @return the device
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
                return "Mobile";
            } else if (userAgent.contains("iPad")) {
                return "iPad";
            }
        }
        return "Desktop";
    }

    /**
     * Gets browser.
     *
     * @param request the request
     * @return the browser
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg") && !userAgent.contains("Firefox")) {
            return "Google chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Edg")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR")) {
            return "Opera";
        }
        return "Other Browser";
    }

    /**
     * Gets jwt from request.
     *
     * @param request the request
     * @return the jwt from request
     */
    public static String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Is null param boolean.
     *
     * @param value the value
     * @return the boolean
     */
    public static boolean isNullParam(String value) {
        return !StringUtils.hasText(value) || "null".equals(value);
    }
}
