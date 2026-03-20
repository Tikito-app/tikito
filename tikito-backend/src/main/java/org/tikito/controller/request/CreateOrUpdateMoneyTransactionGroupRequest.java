package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import org.tikito.dto.money.MoneyTransactionGroupQualifierDto;
import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.money.MoneyTransactionGroupType;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CreateOrUpdateMoneyTransactionGroupRequest extends CreateOrUpdateRequest {
    @NotBlank
    private String name;
    private Set<MoneyTransactionGroupType> groupTypes;
    private List<MoneyTransactionGroupQualifierDto> qualifiers;
    private Set<Long> accountIds;
}
