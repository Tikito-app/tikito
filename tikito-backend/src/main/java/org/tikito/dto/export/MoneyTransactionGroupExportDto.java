package org.tikito.dto.export;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.money.MoneyTransactionGroupType;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MoneyTransactionGroupExportDto {
    @NotBlank
    private String name;
    private Set<MoneyTransactionGroupType> groupTypes;
    private List<@Valid MoneyTransactionGroupQualifierExportDto> qualifiers;
    private Set<String> accountNames;
}
