package com.recruitment.application.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.recruitment.application.exception.BusinessException;
import com.recruitment.application.exception.ErrorCode;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.Optional;

final class DownstreamClientSupport {

    private DownstreamClientSupport() {
    }

    static <T> Optional<T> execute(DownstreamCall<T> call) {
        try {
            return Optional.ofNullable(call.execute());
        } catch (RestClientResponseException exception) {
            HttpStatusCode status = exception.getStatusCode();
            if (status.value() == 404) {
                return Optional.empty();
            }
            if (status.value() == 400) {
                throw new BusinessException(ErrorCode.DOWNSTREAM_BAD_REQUEST);
            }
            if (status.value() == 401) {
                throw new BusinessException(ErrorCode.DOWNSTREAM_UNAUTHORIZED);
            }
            if (status.value() == 403) {
                throw new BusinessException(ErrorCode.DOWNSTREAM_FORBIDDEN);
            }
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        } catch (ResourceAccessException exception) {
            throw new BusinessException(hasTimeoutCause(exception)
                    ? ErrorCode.DOWNSTREAM_TIMEOUT
                    : ErrorCode.DOWNSTREAM_UNAVAILABLE);
        } catch (JsonProcessingException | IllegalStateException exception) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_INVALID_RESPONSE);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_UNAVAILABLE);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.DOWNSTREAM_INVALID_RESPONSE);
        }
    }

    private static boolean hasTimeoutCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException || current instanceof HttpTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @FunctionalInterface
    interface DownstreamCall<T> {
        T execute() throws Exception;
    }
}
