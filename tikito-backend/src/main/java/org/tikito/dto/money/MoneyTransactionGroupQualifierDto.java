package org.tikito.dto.money;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MoneyTransactionGroupQualifierDto {
    private long id;

    private long groupId;

    @NotNull
    private MoneyTransactionGroupQualifierType qualifierType;

    @NotEmpty
    private String qualifier;

    @NotNull
    private MoneyTransactionField transactionField;
}
