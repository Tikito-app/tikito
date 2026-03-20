package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateIsinRequest {
    @NotBlank
    private String symbol;
    private LocalDate validFrom;
    private LocalDate validTo;
}
