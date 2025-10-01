package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.DateRange;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrUpdateLoanRequest {
    private Long id;
    private long loanId;
    @NotNull
    private DateRange dateRange;
    @NotBlank
    private String name;
    private Set<Long> groupIds = new HashSet<>();

    public boolean isNew() {
        return id == null || id == 0;
    }
}
