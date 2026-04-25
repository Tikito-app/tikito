package org.tikito.dto.export;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private Set<@NotNull MoneyTransactionGroupType> groupTypes;
    private List<@NotNull MoneyTransactionGroupQualifierExportDto> qualifiers;
    private Set<@NotNull String> accountNames;
}
