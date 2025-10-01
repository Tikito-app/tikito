package org.tikito.dto.export;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ImportExportSettings {
    private boolean accounts;
    private boolean moneyTransactions;
    private boolean moneyTransactionGroups;
    private boolean securityTransactions;
    private boolean loans;
}
