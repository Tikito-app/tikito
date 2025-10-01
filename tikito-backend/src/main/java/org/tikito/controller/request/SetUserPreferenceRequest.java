package org.tikito.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.UserPreferenceKey;

@Getter
@Setter
@NoArgsConstructor
public class SetUserPreferenceRequest {
    @NotNull
    private UserPreferenceKey key;
    private String value;
}
