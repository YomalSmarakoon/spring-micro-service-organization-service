package com.optimagrowth.organization.utils;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Propagates {@link UserContext} values as HTTP headers on outgoing RestTemplate calls,
 * so correlation ID and other request-scoped metadata cross service boundaries.
 *
 * <p>Wire it into any RestTemplate bean:
 * <pre>
 *   RestTemplate template = new RestTemplate();
 *   template.getInterceptors().add(new UserContextInterceptor());
 * </pre>
 * </p>
 */
public class UserContextInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        UserContext context = UserContextHolder.getContext();

        if (context.getCorrelationId() != null) {
            request.getHeaders().add("correlation-id", context.getCorrelationId());
        }
        if (context.getUserId() != null) {
            request.getHeaders().add("user-id", context.getUserId());
        }
        if (context.getAuthToken() != null) {
            request.getHeaders().add("auth-token", context.getAuthToken());
        }
        if (context.getOrganizationId() != null) {
            request.getHeaders().add("organization-id", context.getOrganizationId());
        }

        return execution.execute(request, body);
    }
}
