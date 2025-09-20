package org.tikito.controller.request;

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
    private Long groupId;
}
