package com.optimagrowth.organization.utils;

public class UserContextHolder {

    private UserContextHolder() {}

    private static final ThreadLocal<UserContext> userContext = ThreadLocal.withInitial(UserContext::new);

    public static UserContext getContext() {
        return userContext.get();
    }

    public static void setContext(UserContext context) {
        userContext.set(context);
    }

    // Prevents memory leaks in thread pools — always call in filter's finally block
    public static void clear() {
        userContext.remove();
    }
}
