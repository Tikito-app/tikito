package org.tikito.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServerResponse<T> {
    private String error;
    private String message;
    private T data;

    public ServerResponse(final String error, final T data) {
        this.error = error;
        this.data = data;
    }

    public ServerResponse(final Exception e) {
        this.error = e.getClass().getSimpleName();
        this.message = e.getMessage();
    }
}
