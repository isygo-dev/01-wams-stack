package eu.isygoit.com.rest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResponseFactory Tests")
class ResponseFactoryTest {

    @Nested
    @DisplayName("Informational (1xx) Responses")
    class InformationalResponses {
        @Test
        @DisplayName("responseContinue should return 100 Continue")
        void testResponseContinue() {
            ResponseEntity<?> response = ResponseFactory.responseContinue();
            assertEquals(HttpStatus.CONTINUE, response.getStatusCode());
        }

        @Test
        @DisplayName("responseSwitchingProtocols should return 101 Switching Protocols")
        void testResponseSwitchingProtocols() {
            ResponseEntity<?> response = ResponseFactory.responseSwitchingProtocols();
            assertEquals(HttpStatus.SWITCHING_PROTOCOLS, response.getStatusCode());
        }

        @Test
        @DisplayName("responseProcessing should return 102 Processing")
        void testResponseProcessing() {
            ResponseEntity<?> response = ResponseFactory.responseProcessing();
            assertEquals(HttpStatus.PROCESSING, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Success (2xx) Responses")
    class SuccessResponses {
        @Test
        @DisplayName("responseOk() should return 200 OK without body")
        void testResponseOkNoBody() {
            ResponseEntity<?> response = ResponseFactory.responseOk();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("responseOk(body) should return 200 OK with body")
        void testResponseOkWithBody() {
            String body = "Success";
            ResponseEntity<String> response = ResponseFactory.responseOk(body);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(body, response.getBody());
        }

        @Test
        @DisplayName("responseCreated(body, location) should return 201 Created with body and location")
        void testResponseCreatedWithLocation() {
            String body = "Created";
            URI location = URI.create("/api/v1/resource/1");
            ResponseEntity<String> response = ResponseFactory.responseCreated(body, location);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(body, response.getBody());
            assertEquals(location, response.getHeaders().getLocation());
        }

        @Test
        @DisplayName("responseCreated(body) should return 201 Created with body")
        void testResponseCreated() {
            String body = "Created";
            ResponseEntity<String> response = ResponseFactory.responseCreated(body);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(body, response.getBody());
        }

        @Test
        @DisplayName("responseNoContent should return 204 No Content")
        void testResponseNoContent() {
            ResponseEntity<?> response = ResponseFactory.responseNoContent();
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("responseAccepted() should return 202 Accepted")
        void testResponseAccepted() {
            ResponseEntity<?> response = ResponseFactory.responseAccepted();
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        }

        @Test
        @DisplayName("responseAccepted(location) should return 202 Accepted with location")
        void testResponseAcceptedWithLocation() {
            URI location = URI.create("/api/v1/resource/1");
            ResponseEntity<?> response = ResponseFactory.responseAccepted(location);
            assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
            assertEquals(location, response.getHeaders().getLocation());
        }

        @Test
        @DisplayName("responsePartialContent(body) should return 206 Partial Content with body")
        void testResponsePartialContent() {
            String body = "Partial";
            ResponseEntity<String> response = ResponseFactory.responsePartialContent(body);
            assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode());
            assertEquals(body, response.getBody());
        }

        @Test
        @DisplayName("responseResetContent() should return 205 Reset Content")
        void testResponseResetContent() {
            ResponseEntity<?> response = ResponseFactory.responseResetContent();
            assertEquals(HttpStatus.RESET_CONTENT, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Redirection (3xx) Responses")
    class RedirectionResponses {
        @Test
        @DisplayName("responseMovedPermanently(location) should return 301 Moved Permanently")
        void testResponseMovedPermanently() {
            URI location = URI.create("http://new-location.com");
            ResponseEntity<?> response = ResponseFactory.responseMovedPermanently(location);
            assertEquals(HttpStatus.MOVED_PERMANENTLY, response.getStatusCode());
            assertEquals(location, response.getHeaders().getLocation());
        }

        @Test
        @DisplayName("responseFound(location) should return 302 Found")
        void testResponseFound() {
            URI location = URI.create("http://found-location.com");
            ResponseEntity<?> response = ResponseFactory.responseFound(location);
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertEquals(location, response.getHeaders().getLocation());
        }

        @Test
        @DisplayName("responseSeeOther(location) should return 303 See Other")
        void testResponseSeeOther() {
            URI location = URI.create("http://see-other.com");
            ResponseEntity<?> response = ResponseFactory.responseSeeOther(location);
            assertEquals(HttpStatus.SEE_OTHER, response.getStatusCode());
            assertEquals(location, response.getHeaders().getLocation());
        }

        @Test
        @DisplayName("responseTemporaryRedirect(location) should return 307 Temporary Redirect")
        void testResponseTemporaryRedirect() {
            URI location = URI.create("http://temp-redirect.com");
            ResponseEntity<?> response = ResponseFactory.responseTemporaryRedirect(location);
            assertEquals(HttpStatus.TEMPORARY_REDIRECT, response.getStatusCode());
            assertEquals(location, response.getHeaders().getLocation());
        }

        @Test
        @DisplayName("responsePermanentRedirect(location) should return 308 Permanent Redirect")
        void testResponsePermanentRedirect() {
            URI location = URI.create("http://perm-redirect.com");
            ResponseEntity<?> response = ResponseFactory.responsePermanentRedirect(location);
            assertEquals(HttpStatus.PERMANENT_REDIRECT, response.getStatusCode());
            assertEquals(location, response.getHeaders().getLocation());
        }
    }

    @Nested
    @DisplayName("Client Error (4xx) Responses")
    class ClientErrorResponses {
        @Test
        @DisplayName("responseBadRequest() should return 400 Bad Request")
        void testResponseBadRequest() {
            ResponseEntity<?> response = ResponseFactory.responseBadRequest();
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("responseBadRequest(body) should return 400 Bad Request with body")
        void testResponseBadRequestWithBody() {
            String body = "Bad Request";
            ResponseEntity<String> response = ResponseFactory.responseBadRequest(body);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(body, response.getBody());
        }

        @Test
        @DisplayName("responseUnauthorized() should return 401 Unauthorized")
        void testResponseUnauthorized() {
            ResponseEntity<?> response = ResponseFactory.responseUnauthorized();
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        @Test
        @DisplayName("responseForbidden() should return 403 Forbidden")
        void testResponseForbidden() {
            ResponseEntity<?> response = ResponseFactory.responseForbidden();
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }

        @Test
        @DisplayName("responseNotFound() should return 404 Not Found")
        void testResponseNotFound() {
            ResponseEntity<?> response = ResponseFactory.responseNotFound();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @DisplayName("responseConflict() should return 409 Conflict")
        void testResponseConflict() {
            ResponseEntity<?> response = ResponseFactory.responseConflict();
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        }

        @Test
        @DisplayName("responseTooManyRequests() should return 429 Too Many Requests")
        void testResponseTooManyRequests() {
            ResponseEntity<?> response = ResponseFactory.responseTooManyRequests();
            assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        }

        @Test
        @DisplayName("responseNotAcceptable(body) should return 406 Not Acceptable with body")
        void testResponseNotAcceptable() {
            String body = "Not Acceptable";
            ResponseEntity<String> response = ResponseFactory.responseNotAcceptable(body);
            assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
            assertEquals(body, response.getBody());
        }

        @Test
        @DisplayName("responseUnprocessableEntity(body) should return 422 Unprocessable Entity with body")
        void testResponseUnprocessableEntity() {
            String body = "Unprocessable";
            ResponseEntity<String> response = ResponseFactory.responseUnprocessableEntity(body);
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
            assertEquals(body, response.getBody());
        }

        @Test
        @DisplayName("responseUnsupportedMediaType() should return 415 Unsupported Media Type")
        void testResponseUnsupportedMediaType() {
            ResponseEntity<?> response = ResponseFactory.responseUnsupportedMediaType();
            assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Server Error (5xx) Responses")
    class ServerErrorResponses {
        @Test
        @DisplayName("responseInternalServerError() should return 500 Internal Server Error")
        void testResponseInternalServerError() {
            ResponseEntity<?> response = ResponseFactory.responseInternalServerError();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        @Test
        @DisplayName("responseInternalServerError(body) should return 500 Internal Server Error with body")
        void testResponseInternalServerErrorWithBody() {
            String body = "Error";
            ResponseEntity<String> response = ResponseFactory.responseInternalServerError(body);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertEquals(body, response.getBody());
        }

        @Test
        @DisplayName("responseNotImplemented() should return 501 Not Implemented")
        void testResponseNotImplemented() {
            ResponseEntity<?> response = ResponseFactory.responseNotImplemented();
            assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
        }

        @Test
        @DisplayName("responseBadGateway() should return 502 Bad Gateway")
        void testResponseBadGateway() {
            ResponseEntity<?> response = ResponseFactory.responseBadGateway();
            assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        }

        @Test
        @DisplayName("responseServiceUnavailable() should return 503 Service Unavailable")
        void testResponseServiceUnavailable() {
            ResponseEntity<?> response = ResponseFactory.responseServiceUnavailable();
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        }

        @Test
        @DisplayName("responseGatewayTimeout() should return 504 Gateway Timeout")
        void testResponseGatewayTimeout() {
            ResponseEntity<?> response = ResponseFactory.responseGatewayTimeout();
            assertEquals(HttpStatus.GATEWAY_TIMEOUT, response.getStatusCode());
        }
    }
}
