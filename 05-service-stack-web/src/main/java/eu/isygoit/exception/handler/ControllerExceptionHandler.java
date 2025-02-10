package eu.isygoit.exception.handler;


import eu.isygoit.exception.ManagedException;
import eu.isygoit.exception.UnknownException;
import eu.isygoit.i18n.service.LocaleService;
import feign.FeignException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.RollbackException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
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
import java.util.Objects;
import java.util.Optional;

/**
 * The type Controller exception handler.
 */
@Slf4j
@Component
public abstract class ControllerExceptionHandler extends ControllerExceptionHandlerBuilder implements IExceptionHandler {

    /**
     * The constant UNKNOWN_REASON.
     */
    public static final String UNKNOWN_REASON = "unknown.reason";
    /**
     * The constant OPERATION_FAILED.
     */
    public static final String OPERATION_FAILED = "operation.failed";
    /**
     * The constant UNMANAGED_EXCEPTION_NOTIFICATION.
     */
    public static final String UNMANAGED_EXCEPTION_NOTIFICATION = "unmanaged.exception.notification";
    private StringBuilder message = new StringBuilder();

    protected abstract LocaleService getLocaleServiceInstance();

    // Returns an Optional containing the ApplicationContextService, if present
    protected Optional<LocaleService> getLocaleService() {
        return Optional.ofNullable(getLocaleServiceInstance());
    }

    public String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    @Override
    public String handleMessage(String message) {
        return getLocaleService().get().getMessage(message, LocaleContextHolder.getLocale());
    }

    @Override
    public String handleError(Throwable throwable) {
        log.error("<Error>: Error in exception handler:", throwable);

        try {
            message.setLength(0);  // Clear the message for each new exception handling
            message.append(handleSpecificExceptions(throwable));
        } catch (Throwable e) {
            message.setLength(0);  // Clear the message for the unmanaged exception
            message.append(getLocaleService().get().getMessage(UNKNOWN_REASON, LocaleContextHolder.getLocale()))
                    .append("\n")
                    .append(getLocaleService().get().getMessage(UNMANAGED_EXCEPTION_NOTIFICATION, LocaleContextHolder.getLocale()))
                    .append("\n");
            processUnmanagedException(this.getStackTrace(e));
        }
        return message.toString();
    }

    private String handleSpecificExceptions(Throwable throwable) {
        if (throwable instanceof SizeLimitExceededException) {
            return getLocaleService().get().getMessage("size.limit.exceeded.exception", LocaleContextHolder.getLocale());
        } else if (throwable instanceof JpaSystemException) {
            throwable = NestedExceptionUtils.getRootCause(throwable);
            return handleJpaSystemException(throwable);
        } else if (throwable instanceof TransactionSystemException) {
            throwable = handleTransactionException(throwable);
            return handleJpaSystemException(throwable);
        } else if (throwable instanceof FeignException) {
            return getLocaleService().get().getMessage(((FeignException) throwable).contentUTF8(), LocaleContextHolder.getLocale());
        } else if (throwable instanceof CannotCreateTransactionException) {
            return getLocaleService().get().getMessage("cannot.create.transaction.exception", LocaleContextHolder.getLocale());
        } else if (throwable instanceof PSQLException) {
            return handleSQLException((PSQLException) throwable);
        } else if (throwable instanceof javax.validation.ConstraintViolationException) {
            return handleConstraintViolationException((javax.validation.ConstraintViolationException) throwable);
        } else if (throwable instanceof EmptyResultDataAccessException) {
            return getLocaleService().get().getMessage("object.not.found", LocaleContextHolder.getLocale());
        } else if (throwable instanceof EntityExistsException) {
            return getLocaleService().get().getMessage("object.already.exists", LocaleContextHolder.getLocale());
        } else if (throwable instanceof PersistenceException || throwable instanceof DataIntegrityViolationException) {
            return handlePersistenceException(throwable);
        } else if (throwable instanceof ManagedException) {
            return getLocaleService().get().getMessage(((ManagedException) throwable).getMsgLocale(), LocaleContextHolder.getLocale());
        } else {
            throw new UnknownException(throwable);
        }
    }

    private String handleJpaSystemException(Throwable throwable) {
        return getLocaleService().get().getMessage("jpa.system.exception", LocaleContextHolder.getLocale());
    }

    private Throwable handleTransactionException(Throwable throwable) {
        if (Objects.nonNull(throwable.getCause()) && throwable.getCause() instanceof RollbackException) {
            return throwable.getCause().getCause();
        }
        return throwable;
    }

    private String handleSQLException(PSQLException throwable) {
        Optional<String> keyOptional = this.getExceptionMessages().keySet().stream()
                .parallel().filter(throwable.getMessage()::contains).findFirst();
        if (keyOptional.isPresent()) {
            return getLocaleService().get().getMessage(this.getExceptionMessages().get(keyOptional.get()), LocaleContextHolder.getLocale());
        } else {
            return getLocaleService().get().getMessage(UNKNOWN_REASON, LocaleContextHolder.getLocale()) + " " + this.getStackTrace(throwable);
        }
    }

    private String handleConstraintViolationException(javax.validation.ConstraintViolationException throwable) {
        StringBuilder localMessage = new StringBuilder();
        if (!CollectionUtils.isEmpty(throwable.getConstraintViolations())) {
            for (ConstraintViolation<?> cv : throwable.getConstraintViolations()) {
                if (StringUtils.hasText(cv.getPropertyPath().toString())) {
                    localMessage.append(getLocaleService().get().getMessage(cv.getPropertyPath().toString().replace("_", "."), LocaleContextHolder.getLocale()))
                            .append(": ");
                }
                localMessage.append(getLocaleService().get().getMessage(cv.getMessage().replace(" ", "."), LocaleContextHolder.getLocale())).append("\n");
            }
        }
        return localMessage.toString();
    }

    private String handlePersistenceException(Throwable throwable) {
        StringBuilder localMessage = new StringBuilder();
        Optional<String> keyOptional = Optional.empty();

        if (Objects.nonNull(throwable.getCause()) && throwable.getCause() instanceof ConstraintViolationException) {
            keyOptional = handleConstraintViolationExceptionCause(throwable);
        }

        if (keyOptional.isPresent()) {
            localMessage.append(getLocaleService().get().getMessage(this.getExceptionMessages().get(keyOptional.get()), LocaleContextHolder.getLocale()));
        } else {
            localMessage.append(getLocaleService().get().getMessage(UNKNOWN_REASON, LocaleContextHolder.getLocale()))
                    .append(" ").append(this.getStackTrace(throwable));
        }
        return localMessage.toString();
    }

    private Optional<String> handleConstraintViolationExceptionCause(Throwable throwable) {
        if (Objects.nonNull(((ConstraintViolationException) throwable.getCause()).getConstraintName())) {
            return this.getExceptionMessages().keySet().stream()
                    .parallel().filter(((ConstraintViolationException) throwable.getCause()).getConstraintName()::equals).findFirst();
        }
        return Optional.empty();
    }
}
