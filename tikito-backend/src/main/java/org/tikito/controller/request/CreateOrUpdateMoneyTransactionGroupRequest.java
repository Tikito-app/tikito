package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import org.tikito.dto.money.MoneyTransactionGroupQualifierDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.money.MoneyTransactionGroupType;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateOrUpdateMoneyTransactionGroupRequest {
    private long id;
    @NotBlank
    private String name;
    private Set<MoneyTransactionGroupType> groupTypes;
    private List<MoneyTransactionGroupQualifierDto> qualifiers;
    private Set<Long> accountIds;
}
