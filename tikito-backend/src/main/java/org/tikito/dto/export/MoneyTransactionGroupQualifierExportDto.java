package org.tikito.dto.export;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.money.MoneyTransactionField;
import org.tikito.dto.money.MoneyTransactionGroupQualifierType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MoneyTransactionGroupQualifierExportDto {

    @NotBlank
    private String groupName;

    @NotNull
    private MoneyTransactionGroupQualifierType qualifierType;

    @NotEmpty
    private String qualifier;

    @NotNull
    private MoneyTransactionField transactionField;
}
