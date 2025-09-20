package org.tikito.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends Exception {
    private final String email;

    public EmailAlreadyExistsException(final String email) {
        this.email = email;
    }
}
