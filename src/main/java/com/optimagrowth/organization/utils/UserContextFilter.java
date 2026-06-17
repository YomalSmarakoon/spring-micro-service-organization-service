package com.optimagrowth.organization.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Populates {@link UserContextHolder} from incoming HTTP headers once per request.
 *
 * <p>Generates a {@code correlation-id} if none is present, and puts it (plus
 * {@code userId} / {@code orgId}) into SLF4J MDC so every log line in this
 * request thread carries those values automatically.</p>
 *
 * <p><b>Important:</b> ThreadLocal context is NOT propagated to other threads
 * ({@code @Async}, {@link java.util.concurrent.CompletableFuture}, parallel
 * streams, WebFlux). Manual propagation is required in those cases.</p>
 */
@Component
public class UserContextFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(UserContextFilter.class);

    private static final String HDR_CORRELATION_ID = "correlation-id";
    private static final String HDR_USER_ID        = "user-id";
    private static final String HDR_AUTH_TOKEN     = "auth-token";
    private static final String HDR_ORG_ID         = "organization-id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            UserContext context = UserContextHolder.getContext();

            String correlationId = headerOrNull(request, HDR_CORRELATION_ID);
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
                LOG.debug("correlation-id generated in UserContextFilter: {}.", correlationId);
            }

            context.setCorrelationId(correlationId);
            context.setUserId(headerOrNull(request, HDR_USER_ID));
            context.setAuthToken(headerOrNull(request, HDR_AUTH_TOKEN));
            context.setOrganizationId(headerOrNull(request, HDR_ORG_ID));

            MDC.put("correlationId", correlationId);
            if (context.getUserId() != null)       MDC.put("userId", context.getUserId());
            if (context.getOrganizationId() != null) MDC.put("orgId", context.getOrganizationId());

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
            UserContextHolder.clear();
        }
    }

    private String headerOrNull(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
