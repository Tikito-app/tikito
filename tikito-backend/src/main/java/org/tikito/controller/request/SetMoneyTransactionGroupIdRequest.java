package org.tikito.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetMoneyTransactionGroupIdRequest {
    private long transactionId;
    @NotNull
    private Long groupId;
}
