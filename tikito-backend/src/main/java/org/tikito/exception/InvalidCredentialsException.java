package org.tikito.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public InvalidCredentialsException(final String msg) {
        super(msg);
    }
}
