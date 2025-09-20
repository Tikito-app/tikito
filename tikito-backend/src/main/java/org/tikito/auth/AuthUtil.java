package org.tikito.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class AuthUtil {
    private AuthUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Optional<AuthUser> getAuthentication() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
            return Optional.of((AuthUser) authentication.getPrincipal());
        }
        return Optional.empty();
    }
}
