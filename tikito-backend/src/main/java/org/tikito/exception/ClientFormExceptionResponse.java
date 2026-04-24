package org.tikito.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ClientFormExceptionResponse {
    private final List<ClientFormValidationExceptionField> errors;

    public ClientFormExceptionResponse(final List<ClientFormValidationExceptionField> errors) {
        this.errors = new ArrayList<>(errors);
    }

    public record ClientFormValidationExceptionField(String field,
                                                     String message) {
    }
}
