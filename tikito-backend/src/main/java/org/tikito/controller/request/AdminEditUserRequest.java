package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEditUserRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
