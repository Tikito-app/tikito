package org.tikito.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetMoneyTransactionGroupIdRequest {
    private long transactionId;
    @NotNull
    private Long groupId;
}
