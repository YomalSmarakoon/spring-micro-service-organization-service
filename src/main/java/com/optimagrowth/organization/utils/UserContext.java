package com.optimagrowth.organization.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class UserContext {

    public static final String CORRELATION_ID = "tmx-correlation-id";
    public static final String AUTH_TOKEN     = "Authorization";
    public static final String USER_ID        = "tmx-user-id";
    public static final String ORG_ID         = "tmx-org-id";

    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    private static final ThreadLocal<String> authToken     = new ThreadLocal<>();
    private static final ThreadLocal<String> userId        = new ThreadLocal<>();
    private static final ThreadLocal<String> orgId         = new ThreadLocal<>();

    public static String getCorrelationId() { return correlationId.get(); }
    public static void setCorrelationId(String cId) { correlationId.set(cId); }

    public static String getAuthToken() { return authToken.get(); }
    public static void setAuthToken(String token) { authToken.set(token); }

    public static String getUserId() { return userId.get(); }
    public static void setUserId(String id) { userId.set(id); }

    public static String getOrgId() { return orgId.get(); }
    public static void setOrgId(String id) { orgId.set(id); }

    public static HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(CORRELATION_ID, getCorrelationId());
        return httpHeaders;
    }
}
