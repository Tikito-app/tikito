package org.tikito.dto.export;

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
    private List<AccountExportDto> accounts;
    private List<MoneyTransactionGroupExportDto> moneyGroups;
    private List<LoanExportDto> loans;
}
