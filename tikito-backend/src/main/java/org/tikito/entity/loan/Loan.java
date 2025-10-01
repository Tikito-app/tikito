package org.tikito.entity.loan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.DateRange;
import org.tikito.dto.export.LoanExportDto;
import org.tikito.dto.loan.LoanDto;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.security.Security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;

    @Enumerated(EnumType.STRING)
    private DateRange dateRange;
    private String name;

    @OneToMany(mappedBy = "loan", fetch = FetchType.LAZY)
    private List<MoneyTransactionGroup> groups = new ArrayList<>();

    @OneToMany(mappedBy = "loan", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanPart> loanParts = new ArrayList<>();

    public Loan(final long userId) {
        this.userId = userId;
    }

    public LoanDto toDto() {
        return new LoanDto(
                id,
                userId,
                dateRange,
                name,
                groups.stream().map(MoneyTransactionGroup::toDto).toList(),
                loanParts.stream().map(LoanPart::toDto).toList());
    }

    public LoanExportDto toExportDto(final Map<Long, Security> currenciesById) {
        return new LoanExportDto();
    }
}
