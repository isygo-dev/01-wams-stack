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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Iterator;
import java.util.Optional;

/**
 * The type Controller exception handler.
 */
@Slf4j
@Data
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

    @Autowired
    private LocaleService localeService;

    public String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    @Override
    public String handleMessage(String message) {
        return getLocaleService().getMessage(message, LocaleContextHolder.getLocale());
    }

    @Override
    public String handleError(Throwable throwable) {
        log.error("<Error>: Error in exception handler:", throwable);
        StringBuilder message = new StringBuilder();
        try {
            if (throwable instanceof SizeLimitExceededException) {
                message.append(localeService.getMessage("size.limit.exceeded.exception", LocaleContextHolder.getLocale()));
            }

            if (throwable instanceof JpaSystemException) {
                throwable = NestedExceptionUtils.getRootCause(throwable);
            }

            if (throwable instanceof TransactionSystemException
                    && throwable.getCause() != null
                    && throwable.getCause().getCause() != null
                    && throwable.getCause() instanceof RollbackException) {
                throwable = throwable.getCause().getCause();
            }

            if (throwable instanceof FeignException) {
                message.append(getLocaleService().getMessage(((FeignException) throwable).contentUTF8(), LocaleContextHolder.getLocale()));
            } else if (throwable instanceof CannotCreateTransactionException) {
                message.append(getLocaleService().getMessage("cannot.create.transaction.exception", LocaleContextHolder.getLocale()));
            } else if (throwable instanceof PSQLException) {
                Optional<String> keyOptional = this.getExcepMessage().keySet().stream().parallel().filter(throwable.getMessage()::contains).findFirst();
                if (keyOptional.isPresent()) {
                    message.append(localeService.getMessage(this.getExcepMessage().get(keyOptional.get()), LocaleContextHolder.getLocale()));
                } else {
                    message.append(localeService.getMessage(UNKNOWN_REASON, LocaleContextHolder.getLocale())).append(" ").append(this.getStackTrace(throwable));
                }
            } else if (throwable instanceof javax.validation.ConstraintViolationException) {
                if (!CollectionUtils.isEmpty(((javax.validation.ConstraintViolationException) throwable).getConstraintViolations())) {
                    Iterator<ConstraintViolation<?>> it = ((javax.validation.ConstraintViolationException) throwable).getConstraintViolations().iterator();
                    while (it.hasNext()) {
                        ConstraintViolation cv = it.next();
                        if (StringUtils.hasText(cv.getPropertyPath().toString())) {
                            message.append(localeService.getMessage(cv.getPropertyPath().toString().replace("_", "."), LocaleContextHolder.getLocale())).append(": ");
                        }
                        message.append(localeService.getMessage(cv.getMessage().replace(" ", "."), LocaleContextHolder.getLocale())).append("\n");
                    }
                }
            } else if (throwable instanceof EmptyResultDataAccessException) {
                message.append(localeService.getMessage("object.not.found", LocaleContextHolder.getLocale()));
            } else if (throwable instanceof EntityExistsException) {
                message.append(localeService.getMessage("object.already.exists", LocaleContextHolder.getLocale()));
            } else if (throwable instanceof PersistenceException
                    || throwable instanceof DataIntegrityViolationException) {
                Optional<String> keyOptional = Optional.empty();
                if (throwable.getCause() != null
                        && throwable.getCause() instanceof ConstraintViolationException) {
                    if (((ConstraintViolationException) throwable.getCause()).getConstraintName() != null) {
                        keyOptional = this.getExcepMessage().keySet().stream().parallel().filter(((ConstraintViolationException) throwable.getCause()).getConstraintName()::equals).findFirst();
                    } else if (throwable.getCause().getCause() != null
                            && throwable.getCause().getCause() instanceof SQLException) {
                        keyOptional = this.getExcepMessage().keySet().stream().parallel().filter(throwable.getCause().getCause().toString().toLowerCase()::equals).findFirst();
                    }

                    if (keyOptional.isPresent()) {
                        message.append(localeService.getMessage(this.getExcepMessage().get(keyOptional.get()), LocaleContextHolder.getLocale()));
                    } else {
                        message.append(localeService.getMessage(UNKNOWN_REASON, LocaleContextHolder.getLocale())).append(" ").append(this.getStackTrace(throwable));
                    }
                } else if (throwable.getCause() != null && throwable.getCause() instanceof DataException) {
                    SQLException sqlException = ((DataException) throwable.getCause()).getSQLException();
                    if (sqlException != null) {
                        message.append(localeService.getMessage(sqlException.getMessage().toLowerCase().replace(" ", ".")
                                        .replace(":", "")
                                        .replace("(", ".")
                                        .replace(")", "")
                                        .replace("error.value.too.long.for.type.character.varying.", "length.must.be.between.0.and.")
                                , LocaleContextHolder.getLocale())).append("\n");
                    }
                } else if (throwable.getCause() != null
                        && throwable.getCause() instanceof DataException
                        && throwable.getCause().getCause() != null) {
                    message.append(localeService.getMessage(UNKNOWN_REASON, LocaleContextHolder.getLocale())).append(" ").append((this.getStackTrace(throwable)));
                } else {
                    message.append(localeService.getMessage(UNKNOWN_REASON, LocaleContextHolder.getLocale())).append(" ").append(this.getStackTrace(throwable));
                }
            } else if (throwable instanceof ManagedException) {
                message.append(getLocaleService().getMessage(((ManagedException) throwable).getMsgLocale(), LocaleContextHolder.getLocale()));
            } else {
                throw new UnknownException(throwable);
            }
        } catch (Throwable e) {
            message.append(getLocaleService().getMessage(UNKNOWN_REASON, LocaleContextHolder.getLocale())).append("\n");
            message.append(getLocaleService().getMessage(UNMANAGED_EXCEPTION_NOTIFICATION, LocaleContextHolder.getLocale())).append("\n");
            processUnmanagedException(this.getStackTrace(e));
        }
        return message.toString();
    }
}
