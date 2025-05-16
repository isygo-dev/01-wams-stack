package eu.isygoit.exception.handler;

import eu.isygoit.exception.ManagedException;
import eu.isygoit.exception.UnknownException;
import eu.isygoit.i18n.service.LocaleService;
import feign.FeignException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.RollbackException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.naming.SizeLimitExceededException;
import javax.validation.ConstraintViolation;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Abstract exception handler for controllers that provides standardized error handling.
 * This class handles various exceptions and produces localized error messages.
 */
@Slf4j
@Data
@Component
public abstract class ControllerExceptionHandler extends ControllerExceptionHandlerBuilder implements IExceptionHandler {

    // Message constants - defined as static final for better performance
    private static final String UNKNOWN_REASON = "unknown.reason";
    private static final String OPERATION_FAILED = "operation.failed";
    private static final String UNMANAGED_EXCEPTION_NOTIFICATION = "unmanaged.exception.notification";
    private static final String SIZE_LIMIT_EXCEEDED = "size.limit.exceeded.exception";
    private static final String CANNOT_CREATE_TRANSACTION = "cannot.create.transaction.exception";
    private static final String OBJECT_NOT_FOUND = "object.not.found";
    private static final String OBJECT_ALREADY_EXISTS = "object.already.exists";

    // Regex patterns for string replacements
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");
    private static final Pattern COLON_PATTERN = Pattern.compile(":");
    private static final Pattern OPEN_PAREN_PATTERN = Pattern.compile("\\(");
    private static final Pattern CLOSE_PAREN_PATTERN = Pattern.compile("\\)");
    private static final Pattern ERROR_VALUE_TOO_LONG_PATTERN =
            Pattern.compile("error\\.value\\.too\\.long\\.for\\.type\\.character\\.varying\\.");

    // Handler cache to improve exception type lookup performance
    private static final Map<Class<?>, Function<Throwable, String>> EXCEPTION_HANDLERS = new ConcurrentHashMap<>();

    @Autowired
    private LocaleService localeService;

    /**
     * Returns a string representation of a throwable's stack trace.
     *
     * @param throwable the throwable to get the stack trace from
     * @return the stack trace as a string
     */
    public String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter(2048); // Pre-size for better performance
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }
        return sw.toString();
    }

    /**
     * Handles localization of a message key.
     *
     * @param message the message key to localize
     * @return the localized message
     */
    @Override
    public String handleMessage(String message) {
        return getLocaleService().getMessage(message, LocaleContextHolder.getLocale());
    }

    /**
     * Main error handling method that processes various exception types
     * and returns appropriate localized error messages.
     *
     * @param throwable the exception to handle
     * @return a localized error message
     */
    @Override
    public String handleError(Throwable throwable) {
        log.debug("Handling exception of type: {}", throwable.getClass().getName());

        // Initialize string builder with capacity to avoid resizing
        StringBuilder message = new StringBuilder(256);

        try {
            throwable = unwrapException(throwable);

            // Get current locale once to avoid repeated calls
            Locale currentLocale = LocaleContextHolder.getLocale();

            // Handle the exception based on its type
            if (throwable instanceof SizeLimitExceededException) {
                message.append(localeService.getMessage(SIZE_LIMIT_EXCEEDED, currentLocale));
            } else if (throwable instanceof FeignException feignEx) {
                message.append(getLocaleService().getMessage(feignEx.contentUTF8(), currentLocale));
            } else if (throwable instanceof CannotCreateTransactionException) {
                message.append(getLocaleService().getMessage(CANNOT_CREATE_TRANSACTION, currentLocale));
            } else if (throwable instanceof PSQLException) {
                handlePSQLException(throwable, message, currentLocale);
            } else if (throwable instanceof javax.validation.ConstraintViolationException validationEx) {
                handleConstraintViolation(validationEx, message, currentLocale);
            } else if (throwable instanceof EmptyResultDataAccessException) {
                message.append(localeService.getMessage(OBJECT_NOT_FOUND, currentLocale));
            } else if (throwable instanceof EntityExistsException) {
                message.append(localeService.getMessage(OBJECT_ALREADY_EXISTS, currentLocale));
            } else if (throwable instanceof PersistenceException ||
                    throwable instanceof DataIntegrityViolationException) {
                handlePersistenceException(throwable, message, currentLocale);
            } else if (throwable instanceof ManagedException managedEx) {
                message.append(getLocaleService().getMessage(managedEx.getMsgLocale(), currentLocale));
            } else {
                throw new UnknownException(throwable);
            }
        } catch (Throwable e) {
            // Handle any unexpected errors in the exception handling process
            Locale currentLocale = LocaleContextHolder.getLocale();
            message.append(getLocaleService().getMessage(UNKNOWN_REASON, currentLocale)).append('\n');
            message.append(getLocaleService().getMessage(UNMANAGED_EXCEPTION_NOTIFICATION, currentLocale)).append('\n');

            // Log the original error and the error in exception handling
            log.error("Exception handler failed to process: {} with secondary exception: {}",
                    throwable.getClass().getName(), e.getClass().getName(), e);

            processUnmanagedException(getStackTrace(e));
        }

        return message.toString();
    }

    /**
     * Unwraps nested exceptions to get to the root cause.
     *
     * @param throwable the exception to unwrap
     * @return the unwrapped exception
     */
    private Throwable unwrapException(Throwable throwable) {
        // Unwrap JPA system exceptions
        if (throwable instanceof JpaSystemException) {
            return NestedExceptionUtils.getRootCause(throwable);
        }

        // Unwrap transaction exceptions
        if (throwable instanceof TransactionSystemException &&
                throwable.getCause() != null &&
                throwable.getCause().getCause() != null &&
                throwable.getCause() instanceof RollbackException) {
            return throwable.getCause().getCause();
        }

        return throwable;
    }

    /**
     * Handles PostgreSQL exceptions.
     */
    private void handlePSQLException(Throwable throwable, StringBuilder message, Locale locale) {
        Optional<String> keyOptional = getExcepMessage().keySet().stream()
                .filter(throwable.getMessage()::contains)
                .findFirst();

        if (keyOptional.isPresent()) {
            message.append(localeService.getMessage(getExcepMessage().get(keyOptional.get()), locale));
        } else {
            message.append(localeService.getMessage(UNKNOWN_REASON, locale))
                    .append(' ')
                    .append(getStackTrace(throwable));
        }
    }

    /**
     * Handles Bean Validation constraint violations.
     */
    private void handleConstraintViolation(javax.validation.ConstraintViolationException validationEx,
                                           StringBuilder message, Locale locale) {
        if (CollectionUtils.isEmpty(validationEx.getConstraintViolations())) {
            return;
        }

        for (ConstraintViolation<?> cv : validationEx.getConstraintViolations()) {
            if (StringUtils.hasText(cv.getPropertyPath().toString())) {
                message.append(localeService.getMessage(
                                cv.getPropertyPath().toString().replace('_', '.'),
                                locale))
                        .append(": ");
            }

            message.append(localeService.getMessage(
                            SPACE_PATTERN.matcher(cv.getMessage()).replaceAll("."),
                            locale))
                    .append('\n');
        }
    }

    /**
     * Handles persistence exceptions and data integrity violations.
     */
    private void handlePersistenceException(Throwable throwable, StringBuilder message, Locale locale) {
        if (throwable.getCause() instanceof ConstraintViolationException) {
            handleConstraintViolationException((ConstraintViolationException) throwable.getCause(), message, locale);
        } else if (throwable.getCause() instanceof DataException) {
            handleDataException((DataException) throwable.getCause(), message, locale);
        } else {
            message.append(localeService.getMessage(UNKNOWN_REASON, locale))
                    .append(' ')
                    .append(getStackTrace(throwable));
        }
    }

    /**
     * Handles constraint violation exceptions.
     */
    private void handleConstraintViolationException(ConstraintViolationException ex, StringBuilder message, Locale locale) {
        Optional<String> keyOptional = Optional.empty();

        if (ex.getConstraintName() != null) {
            keyOptional = getExcepMessage().keySet().stream()
                    .filter(ex.getConstraintName()::equals)
                    .findFirst();
        } else if (ex.getCause() instanceof SQLException) {
            keyOptional = getExcepMessage().keySet().stream()
                    .filter(ex.getCause().toString().toLowerCase()::equals)
                    .findFirst();
        }

        if (keyOptional.isPresent()) {
            message.append(localeService.getMessage(getExcepMessage().get(keyOptional.get()), locale));
        } else {
            message.append(localeService.getMessage(UNKNOWN_REASON, locale))
                    .append(' ')
                    .append(getStackTrace(ex));
        }
    }

    /**
     * Handles data exceptions.
     */
    private void handleDataException(DataException ex, StringBuilder message, Locale locale) {
        SQLException sqlException = ex.getSQLException();
        if (sqlException != null) {
            String msgKey = sqlException.getMessage().toLowerCase();
            msgKey = SPACE_PATTERN.matcher(msgKey).replaceAll(".");
            msgKey = COLON_PATTERN.matcher(msgKey).replaceAll("");
            msgKey = OPEN_PAREN_PATTERN.matcher(msgKey).replaceAll(".");
            msgKey = CLOSE_PAREN_PATTERN.matcher(msgKey).replaceAll("");
            msgKey = ERROR_VALUE_TOO_LONG_PATTERN.matcher(msgKey)
                    .replaceAll("length.must.be.between.0.and.");

            message.append(localeService.getMessage(msgKey, locale)).append('\n');
        } else {
            message.append(localeService.getMessage(UNKNOWN_REASON, locale))
                    .append(' ')
                    .append(getStackTrace(ex));
        }
    }
}