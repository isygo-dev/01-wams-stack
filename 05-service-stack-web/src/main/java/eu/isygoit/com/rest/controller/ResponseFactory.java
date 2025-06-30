package eu.isygoit.com.rest.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

/**
 * Factory class to build standard HTTP responses.
 */
public class ResponseFactory {

    // region 1xx Informational

    /**
     * Response continue response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseContinue() {
        return new ResponseEntity<>(HttpStatus.CONTINUE);
    }

    /**
     * Response switching protocols response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseSwitchingProtocols() {
        return new ResponseEntity<>(HttpStatus.SWITCHING_PROTOCOLS);
    }

    /**
     * Response processing response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseProcessing() {
        return new ResponseEntity<>(HttpStatus.PROCESSING);
    }

    // endregion

    // region 2xx Success

    /**
     * Response ok response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseOk() {
        return ResponseEntity.ok().build();
    }

    /**
     * Response ok response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseOk(T body) {
        return ResponseEntity.ok(body);
    }

    /**
     * Response created response entity.
     *
     * @param <T>      the type parameter
     * @param body     the body
     * @param location the location
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseCreated(T body, URI location) {
        return ResponseEntity.created(location).body(body);
    }

    /**
     * Response created response entity.
     *
     * @param <T>      the type parameter
     * @param body     the body
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseCreated(T body) {
        return ResponseEntity.created(null).body(body);
    }

    /**
     * Response no content response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseNoContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Response accepted response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseAccepted() {
        return ResponseEntity.accepted().build();
    }

    /**
     * Response accepted response entity.
     *
     * @param <T>      the type parameter
     * @param location the location
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseAccepted(URI location) {
        return ResponseEntity.accepted().location(location).build();
    }

    /**
     * Response partial content response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responsePartialContent(T body) {
        return new ResponseEntity<>(body, HttpStatus.PARTIAL_CONTENT);
    }

    /**
     * Response reset content response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseResetContent() {
        return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
    }

    // endregion

    // region 3xx Redirection

    /**
     * Response moved permanently response entity.
     *
     * @param <T>      the type parameter
     * @param location the location
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseMovedPermanently(URI location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    /**
     * Response found response entity.
     *
     * @param <T>      the type parameter
     * @param location the location
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseFound(URI location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Response see other response entity.
     *
     * @param <T>      the type parameter
     * @param location the location
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseSeeOther(URI location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
    }

    /**
     * Response temporary redirect response entity.
     *
     * @param <T>      the type parameter
     * @param location the location
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseTemporaryRedirect(URI location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
    }

    /**
     * Response permanent redirect response entity.
     *
     * @param <T>      the type parameter
     * @param location the location
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responsePermanentRedirect(URI location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(headers, HttpStatus.PERMANENT_REDIRECT);
    }

    // endregion

    // region 4xx Client Errors

    /**
     * Response bad request response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseBadRequest() {
        return ResponseEntity.badRequest().build();
    }

    /**
     * Response bad request response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseBadRequest(T body) {
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Response unauthorized response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseUnauthorized() {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Response forbidden response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseForbidden() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    /**
     * Response not found response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseNotFound() {
        return ResponseEntity.notFound().build();
    }

    /**
     * Response conflict response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseConflict() {
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    /**
     * Response too many requests response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseTooManyRequests() {
        return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Response not acceptable response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseNotAcceptable(T body) {
        return new ResponseEntity<>(body, HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Response unprocessable entity response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseUnprocessableEntity(T body) {
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Response unsupported media type response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseUnsupportedMediaType() {
        return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // endregion

    // region 5xx Server Errors

    /**
     * Response internal server error response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseInternalServerError() {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Response internal server error response entity.
     *
     * @param <T>  the type parameter
     * @param body the body
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseInternalServerError(T body) {
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Response not implemented response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseNotImplemented() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * Response bad gateway response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseBadGateway() {
        return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
    }

    /**
     * Response service unavailable response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseServiceUnavailable() {
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Response gateway timeout response entity.
     *
     * @param <T> the type parameter
     * @return the response entity
     */
    public static <T> ResponseEntity<T> responseGatewayTimeout() {
        return new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
    }

    // endregion
}