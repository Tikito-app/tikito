package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateIsinRequest {
    @NotBlank
    private String symbol;
    private LocalDate validFrom;
    private LocalDate validTo;
}
