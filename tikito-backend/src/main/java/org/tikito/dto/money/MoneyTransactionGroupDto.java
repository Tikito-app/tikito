package org.tikito.dto.money;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MoneyTransactionGroupDto {
    private long id;
    private String name;
    private List<MoneyTransactionGroupQualifierDto> qualifiers;
}
