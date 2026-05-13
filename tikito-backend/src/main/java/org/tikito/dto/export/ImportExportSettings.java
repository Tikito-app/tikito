package org.tikito.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImportExportSettings {
    private @NotNull Boolean accounts;
    private @NotNull Boolean moneyTransactions;
    private @NotNull Boolean moneyTransactionGroups;
    private @NotNull Boolean securityTransactions;
    private @NotNull Boolean loans;
}
