package eu.isygoit.annotation;

import org.springframework.http.HttpStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Msg locale.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MsgLocale {

    /**
     * Http status http status.
     *
     * @return the http status
     */
    HttpStatus httpStatus() default HttpStatus.INTERNAL_SERVER_ERROR; // HTTP status code;

    /**
     * Value string.
     *
     * @return the string
     */
    String value(); // message key
}
