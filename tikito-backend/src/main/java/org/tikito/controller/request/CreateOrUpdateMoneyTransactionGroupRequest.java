package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.DateRange;
import org.tikito.dto.money.MoneyTransactionGroupQualifierDto;
import org.tikito.dto.money.MoneyTransactionGroupType;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CreateOrUpdateMoneyTransactionGroupRequest extends CreateOrUpdateRequest {
    @NotBlank
    private String name;
    @NotNull
    private Set<@NotNull MoneyTransactionGroupType> groupTypes;
    private List<@NotNull MoneyTransactionGroupQualifierDto> qualifiers;
    @NotNull
    private Set<@NotNull Long> accountIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private DateRange dateRange;
    private Integer dateRangeAmount;
    private Double budgeted;
}
