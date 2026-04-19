package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
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
    private Set<MoneyTransactionGroupType> groupTypes;
    private List<MoneyTransactionGroupQualifierDto> qualifiers;
    private Set<Long> accountIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private DateRange dateRange;
    private Integer dateRangeAmount;
    private Double budgeted;
}
