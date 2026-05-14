package org.tikito.exception;

import java.util.ArrayList;
import java.util.List;

public record ClientFormExceptionResponse(List<ClientFormValidationExceptionField> errors) {
    public ClientFormExceptionResponse(final List<ClientFormValidationExceptionField> errors) {
        this.errors = new ArrayList<>(errors);
    }

    public record ClientFormValidationExceptionField(String field,
                                                     String message) {
    }
}
