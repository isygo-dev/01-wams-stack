package eu.isygoit.com.rest.controller.constants;

import java.util.regex.Pattern;

/**
 * The type Ctrl constants.
 */
public interface CtrlConstants {

    String ERROR_API_EXCEPTION = "<Error>: failed with exception : {} ";
    String CREATE_DATE_FIELD = "createDate";
    int DEFAULT_PAGE_SIZE = 20;
    int MAX_PAGE_SIZE = 100;
    int DEFAULT_PAGE = 0;
    String ERROR_BEAN_NOT_FOUND = "<Error>: bean {} not found";
    String CONTROLLER_SERVICE = "controller api";
    String SHOULD_USE_SAAS_SPECIFIC_METHOD = "should use SAAS-specific method";
    String UNKNOWN_REASON = "unknown.reason";
    String OPERATION_FAILED = "operation.failed";
    String UNMANAGED_EXCEPTION_NOTIFICATION = "unmanaged.exception.notification";
    String SIZE_LIMIT_EXCEEDED = "size.limit.exceeded.exception";
    String CANNOT_CREATE_TRANSACTION = "cannot.create.transaction.exception";
    String OBJECT_NOT_FOUND = "object.not.found";
    String OBJECT_ALREADY_EXISTS = "object.already.exists";

    // Regex patterns for string replacements
    Pattern SPACE_PATTERN = Pattern.compile(" ");
    Pattern COLON_PATTERN = Pattern.compile(":");
    Pattern OPEN_PAREN_PATTERN = Pattern.compile("\\(");
    Pattern CLOSE_PAREN_PATTERN = Pattern.compile("\\)");
    Pattern ERROR_VALUE_TOO_LONG_PATTERN = Pattern.compile("error\\.value\\.too\\.long\\.for\\.type\\.character\\.varying\\.");
}
