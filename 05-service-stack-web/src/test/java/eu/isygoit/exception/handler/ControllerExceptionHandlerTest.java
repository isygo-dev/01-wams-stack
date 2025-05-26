package eu.isygoit.exception.handler;

import eu.isygoit.exception.ManagedException;
import eu.isygoit.i18n.service.LocaleService;
import feign.FeignException;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.postgresql.util.PSQLException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.naming.SizeLimitExceededException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The type Controller exception handler test.
 */
@Slf4j
class ControllerExceptionHandlerTest {

    private static MockedStatic<LocaleContextHolder> localeContextHolderMock;
    private final Locale testLocale = Locale.ENGLISH;
    private ControllerExceptionHandler handler;
    private LocaleService localeServiceMock;

    /**
     * Before all.
     */
    @BeforeAll
    static void beforeAll() {
        localeContextHolderMock = Mockito.mockStatic(LocaleContextHolder.class);
    }

    /**
     * After all.
     */
    @AfterAll
    static void afterAll() {
        localeContextHolderMock.close();
    }

    /**
     * Sets .
     */
    @BeforeEach
    void setup() {
        localeServiceMock = mock(LocaleService.class);
        localeContextHolderMock.when(LocaleContextHolder::getLocale).thenReturn(testLocale);

        handler = new ControllerExceptionHandler() {
            @Override
            public java.util.Map<String, String> getExcepMessage() {
                return Collections.singletonMap("some_constraint", "localized.constraint.message");
            }

            @Override
            public void processUnmanagedException(String stackTrace) {
                log.info("Email will be sent tou the admin");
            }
        };
        handler.setLocaleService(localeServiceMock);
    }

    /**
     * Test handle error size limit exceeded exception.
     */
    @Test
    void testHandleError_sizeLimitExceededException() {
        when(localeServiceMock.getMessage("size.limit.exceeded.exception", testLocale))
                .thenReturn("Size limit exceeded");

        String result = handler.handleError(new SizeLimitExceededException("Limit"));

        assertTrue(result.contains("Size limit exceeded"));
    }

    /**
     * Test handle error feign exception.
     */
    @Test
    void testHandleError_feignException() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.contentUTF8()).thenReturn("feign error");
        when(localeServiceMock.getMessage("feign error", testLocale)).thenReturn("Feign localized message");

        String result = handler.handleError(feignEx);

        assertTrue(result.contains("Feign localized message"));
    }

    /**
     * Test handle error cannot create transaction exception.
     */
    @Test
    void testHandleError_cannotCreateTransactionException() {
        when(localeServiceMock.getMessage("cannot.create.transaction.exception", testLocale))
                .thenReturn("Cannot create transaction");

        String result = handler.handleError(new CannotCreateTransactionException("error"));

        assertTrue(result.contains("Cannot create transaction"));
    }

    /**
     * Test handle error psql exception with constraint name.
     */
    @Test
    void testHandleError_psqlException_withConstraintName() {
        PSQLException psqlEx = mock(PSQLException.class);
        when(psqlEx.getMessage()).thenReturn("some_constraint violation");

        when(localeServiceMock.getMessage("localized.constraint.message", testLocale))
                .thenReturn("Localized constraint message");

        String result = handler.handleError(psqlEx);

        assertTrue(result.contains("Localized constraint message"));
    }

    /**
     * Test handle error constraint violation exception.
     */
    @Test
    void testHandleError_constraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);

        // Mock Path object to return the property path string
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("property.path");

        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getMessage()).thenReturn("must not be null");

        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);
        ConstraintViolationException validationEx = new ConstraintViolationException("validation error", violations);

        when(localeServiceMock.getMessage("property.path", testLocale)).thenReturn("Property path");
        when(localeServiceMock.getMessage("must.not.be.null", testLocale)).thenReturn("Must not be null");

        String result = handler.handleError(validationEx);

        assertTrue(result.contains("Property path"));
        assertTrue(result.contains("Must not be null"));
    }

    /**
     * Test handle error entity exists exception.
     */
    @Test
    void testHandleError_entityExistsException() {
        when(localeServiceMock.getMessage("object.already.exists", testLocale))
                .thenReturn("Object already exists");

        String result = handler.handleError(new EntityExistsException());

        assertTrue(result.contains("Object already exists"));
    }

    /**
     * Test handle error constraint violation exception validation.
     */
    @Test
    void testHandleError_ConstraintViolationException_Validation() {
        javax.validation.ConstraintViolation<?> violation = mock(javax.validation.ConstraintViolation.class);
        // Mock Path object
        var mockPath = mock(javax.validation.Path.class);
        when(mockPath.toString()).thenReturn("property.path");
        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getMessage()).thenReturn("must not be null");

        Set<javax.validation.ConstraintViolation<?>> violations = Collections.singleton(violation);
        javax.validation.ConstraintViolationException validationEx = new javax.validation.ConstraintViolationException("validation error", violations);

        when(localeServiceMock.getMessage("property.path", testLocale)).thenReturn("Property path");
        when(localeServiceMock.getMessage("must.not.be.null", testLocale)).thenReturn("Must not be null");

        String result = handler.handleError(validationEx);

        assertTrue(result.contains("Property path"));
        assertTrue(result.contains("Must not be null"));
    }

    /**
     * Test handle error managed exception.
     */
    @Test
    void testHandleError_managedException() {
        ManagedException managedEx = mock(ManagedException.class);
        when(managedEx.getMsgLocale()).thenReturn("managed.error");
        when(localeServiceMock.getMessage("managed.error", testLocale))
                .thenReturn("Managed localized message");

        String result = handler.handleError(managedEx);

        assertTrue(result.contains("Managed localized message"));
    }

    /**
     * Test handle error unknown exception fallback.
     */
    @Test
    void testHandleError_unknownExceptionFallback() {
        RuntimeException unknown = new RuntimeException("unknown error");

        when(localeServiceMock.getMessage("unknown.reason", testLocale)).thenReturn("Unknown reason");
        when(localeServiceMock.getMessage("unmanaged.exception.notification", testLocale))
                .thenReturn("Unmanaged exception notification");

        String result = handler.handleError(unknown);

        assertTrue(result.contains("Unknown reason"));
        assertTrue(result.contains("Unmanaged exception notification"));
    }
}