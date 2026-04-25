package org.tikito.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.DateRange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanExportDto {
    @NotNull
    private String name;
    private DateRange dateRange;
    private Set<String> groups = new HashSet<>();
    private List<LoanPartExportDto> loanParts = new ArrayList<>();
}
