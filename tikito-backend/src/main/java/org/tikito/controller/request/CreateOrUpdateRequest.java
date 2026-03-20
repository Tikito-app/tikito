package org.tikito.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CreateOrUpdateRequest {
    protected Long id;

    public boolean isNew() {
        return id == null || id < 1;
    }
}
