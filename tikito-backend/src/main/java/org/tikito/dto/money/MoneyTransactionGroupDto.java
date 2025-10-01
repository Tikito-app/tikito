package org.tikito.dto.money;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class MoneyTransactionGroupDto {
    private long id;
    private long userId;
    private String name;
    private Set<MoneyTransactionGroupType> groupTypes;
    private List<MoneyTransactionGroupQualifierDto> qualifiers;
    private Set<Long> accountIds;
}
