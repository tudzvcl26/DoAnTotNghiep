package com.recruitment.gateway.exception;

import io.netty.channel.ConnectTimeoutException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

@Component
@Order(-2)
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final GatewayResponseWriter responseWriter;

    public GatewayErrorWebExceptionHandler(GatewayResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable error) {
        Throwable cause = rootCause(Exceptions.unwrap(error));
        if (isTimeout(cause)) {
            return responseWriter.write(exchange, HttpStatus.GATEWAY_TIMEOUT,
                    "GATEWAY_TIMEOUT", "The upstream service did not respond in time");
        }
        if (cause instanceof ConnectException || cause instanceof ConnectTimeoutException
                || cause instanceof UnknownHostException) {
            return responseWriter.write(exchange, HttpStatus.SERVICE_UNAVAILABLE,
                    "GATEWAY_UPSTREAM_UNAVAILABLE", "The upstream service is unavailable");
        }
        if (cause instanceof ResponseStatusException responseStatusException) {
            HttpStatus status = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            if (status != null) {
                return responseWriter.write(exchange, status, "GATEWAY_REQUEST_REJECTED",
                        "The gateway could not process the request");
            }
        }
        if (cause instanceof PrematureCloseException) {
            return responseWriter.write(exchange, HttpStatus.BAD_GATEWAY,
                    "GATEWAY_BAD_RESPONSE", "The upstream service closed the connection unexpectedly");
        }
        return responseWriter.write(exchange, HttpStatus.BAD_GATEWAY,
                "GATEWAY_ERROR", "The gateway could not complete the upstream request");
    }

    private boolean isTimeout(Throwable error) {
        return error instanceof TimeoutException
                || error instanceof io.netty.handler.timeout.ReadTimeoutException
                || error instanceof io.netty.handler.timeout.WriteTimeoutException
                || error.getClass().getSimpleName().contains("Timeout");
    }

    private Throwable rootCause(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
