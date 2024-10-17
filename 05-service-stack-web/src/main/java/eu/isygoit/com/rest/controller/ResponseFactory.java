package eu.isygoit.com.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

/*
***************************************************
1×× Informational
***************************************************
100 Continue
101 Switching Protocols
102 Processing
***************************************************
2×× Success
***************************************************
200 OK
201 Created
202 Accepted
203 Non-authoritative Information
204 No Content
205 Reset Content
206 Partial Content
207 Multi-Status
208 Already Reported
226 IM Used
***************************************************
3×× Redirection
***************************************************
300 Multiple Choices
301 Moved Permanently
302 Found
303 See Other
304 Not Modified
305 Use Proxy
307 Temporary Redirect
308 Permanent Redirect
***************************************************
4×× Client Error
***************************************************
400 Bad Request
401 Unauthorized
402 Payment Required
403 Forbidden
404 Not Found
405 Method Not Allowed
406 Not Acceptable
407 Proxy Authentication Required
408 Request Timeout
409 Conflict
410 Gone
411 Length Required
412 Precondition Failed
413 Payload Too Large
414 Request-URI Too Long
415 Unsupported Media Type
416 Requested Range Not Satisfiable
417 Expectation Failed
418 I'm a teapot
421 Misdirected Request
422 Unprocessable IEntity
423 Locked
424 Failed Dependency
426 Upgrade Required
428 Precondition Required
429 Too Many Requests
431 Request Header Fields Too Large
444 Connection Closed Without Response
451 Unavailable For Legal Reasons
499 Client Closed Request
***************************************************
5×× Server Error
***************************************************
500 Internal Server Error
501 Not Implemented
502 Bad Gateway
503 Service Unavailable
504 Gateway Timeout
505 HTTP Version Not Supported
506 Variant Also Negotiates
507 Insufficient Storage
508 Loop Detected
510 Not Extended
511 Network Authentication Required
599 Network Connect Timeout Error
 */

/**
 * The type Response factory.
 */
public class ResponseFactory {

    /**
     * Response accepted response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    /*
     * 202 OK.
     */
    public static <T> ResponseEntity<T> ResponseAccepted() {
        return ResponseEntity.accepted().build();
    }

    /**
     * Response accepted response entity.
     *
     * @param <T>      the type parameter
     * @param location the location
     * @return the response entity
     */
    /*
     * 202 OK.
     */
    public static <T> ResponseEntity<T> ResponseAccepted(URI location) {
        return ResponseEntity.accepted().location(location).build();
    }

    /**
     * Response ok response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    /*
     * 200 OK.
     */
    public static <T> ResponseEntity<T> ResponseOk() {
        return ResponseEntity.ok().build();
    }

    /**
     * Response ok response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    /*
     * 200 OK.
     */
    public static <T> ResponseEntity<T> ResponseOk(T body) {
        return ResponseEntity.ok(body);
    }

    /**
     * Response ok response entity.
     *
     * @param <T>          the type parameter
     * @param body         the body
     * @param headerName   the header name
     * @param headerValues the header values
     * @return the response entity
     */
    /*
     * 200 OK.
     */
    public static <T> ResponseEntity<T> ResponseOk(T body, String headerName, String... headerValues) {
        return ResponseEntity.ok().header(headerName, headerValues).body(body);
    }

    /**
     * Response created response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    /*
     * 201 Created.
     */
    public static <T> ResponseEntity<T> ResponseCreated(T body) {
        return ResponseEntity.created(null).body(body);
    }

    /**
     * Response created redirection response entity.
     *
     * @param <T>      the type parameter
     * @param location the location
     * @param body     the body
     * @return the response entity
     */
    public static <T> ResponseEntity<T> ResponseCreatedRedirection(URI location, T body) {
        return ResponseEntity.created(location).body(body);
    }

    /**
     * Response no content response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    /*
     * 204 No Content.
     */
    public static <T> ResponseEntity<T> ResponseNoContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Response bad request response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    /*
     * 400 Bad request
     */
    public static <T> ResponseEntity<T> ResponseBadRequest() {
        return ResponseEntity.badRequest().build();
    }

    /**
     * Response bad request response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    /*
     * 400 Bad request
     */
    public static <T> ResponseEntity<T> ResponseBadRequest(T body) {
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Response not found response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    /*
     * 404 Not Found
     */
    public static <T> ResponseEntity<T> ResponseNotFound() {
        return ResponseEntity.notFound().build();
    }

    /**
     * Response not acceptable response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    /*
     * 406 Not Acceptable.
     */
    public static <T> ResponseEntity<T> ResponseNotAcceptable(T body) {
        return new ResponseEntity<T>(body, HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Response internal error response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    /*
     * 500 Internal Server Error.
     */
    public static <T> ResponseEntity<T> ResponseInternalError() {
        return new ResponseEntity<T>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Response internal error response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    /*
     * 500 Internal Server Error.
     */
    public static <T> ResponseEntity<T> ResponseInternalError(T body) {
        return new ResponseEntity<T>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
