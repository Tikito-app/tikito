package org.tikito.controller.request;

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
    private String symbol;
    private LocalDate validFrom;
    private LocalDate validTo;
}
