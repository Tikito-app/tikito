package org.tikito.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TikitoExportDto {
    private List<@NotNull AccountExportDto> accounts;
    private List<@NotNull MoneyTransactionGroupExportDto> moneyGroups;
    private List<@NotNull LoanExportDto> loans;
}
