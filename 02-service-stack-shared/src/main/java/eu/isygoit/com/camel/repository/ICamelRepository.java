package eu.isygoit.com.camel.repository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The interface Camel repository.
 */
public interface ICamelRepository {

    /**
     * The constant send_socket_queue.
     */
    String send_socket_queue = "send-socket-queue";
    /**
     * The constant connect_chat_queue.
     */
    String connect_chat_queue = "connect-chat-queue";
    /**
     * The constant subscribe_chat_queue.
     */
    String subscribe_chat_queue = "subscribe-chat-queue";
    /**
     * The constant send_email_queue.
     */
    String send_email_queue = "send-email-queue";
    /**
     * The constant parse_resume_queue.
     */
    String parse_resume_queue = "parse-resume-queue";

    /**
     * The constant timeline_queue.
     */
    String timeline_queue = "timeline-queue";

    /**
     * Async send body completable future.
     *
     * @param endpointUri the endpoint uri
     * @param body        the body
     * @return the completable future
     */
    CompletableFuture<Object> asyncSendBody(String endpointUri, Object body);

    /**
     * Async request body and header completable future.
     *
     * @param endpointUri the endpoint uri
     * @param body        the body
     * @param header      the header
     * @param headerValue the header value
     * @return the completable future
     */
    CompletableFuture<Object> asyncRequestBodyAndHeader(String endpointUri, Object body, String header, Object headerValue);

    /**
     * Async request body and headers completable future.
     *
     * @param endpointUri the endpoint uri
     * @param body        the body
     * @param headers     the headers
     * @return the completable future
     */
    CompletableFuture<Object> asyncRequestBodyAndHeaders(String endpointUri, Object body, Map<String, Object> headers);
}
