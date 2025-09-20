package org.tikito.controller.request;

import org.tikito.dto.money.MoneyTransactionGroupQualifierDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateOrUpdateMoneyTransactionGroupRequest {
    private long id;
    //
//    @NotEmpty
    private String name;
    private List<MoneyTransactionGroupQualifierDto> qualifiers;
}
