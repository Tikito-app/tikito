package org.tikito.exception;

import org.springframework.security.core.AuthenticationException;

public class PasswordNotLongEnoughException extends AuthenticationException {
    public PasswordNotLongEnoughException(final String msg) {
        super(msg);
    }
}
