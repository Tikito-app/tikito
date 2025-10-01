package org.tikito.entity.loan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.export.LoanPartExportDto;
import org.tikito.dto.loan.LoanPartDto;
import org.tikito.dto.loan.LoanType;
import org.tikito.entity.security.Security;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LoanPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private double amount; // initial amount
    private double remainingAmount;
    private double periodicPayment; // e.g. annuiteit
    private long currencyId;
    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @OneToMany(mappedBy = "loanPart", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanInterest> interests = new ArrayList<>();

    public LoanPart(final long userId, final Loan loan) {
        this.userId = userId;
        this.loan = loan;
    }

    public LoanPart(final long userId, final LoanPartExportDto dto, final Map<String, Security> currenciesByIsin, final Loan loan) {
        this.userId = userId;
        this.name = dto.getName();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.amount = dto.getAmount();
        this.currencyId = currenciesByIsin.get(dto.getCurrency()).getId();
        this.loanType = dto.getLoanType();
        this.loan = loan;
        this.interests = new ArrayList<>(dto
                .getInterests()
                .stream()
                .map(interest -> new LoanInterest(interest, this))
                .toList());
    }

    public LoanPartDto toDto() {
        return new LoanPartDto(
                id,
                loan.getId(),
                userId,
                name,
                startDate,
                endDate,
                amount,
                remainingAmount,
                periodicPayment,
                currencyId,
                loanType,
                interests.stream().map(LoanInterest::toDto).toList());
    }

    public LoanPartExportDto toExportDto(final Map<Long, Security> currenciesById) {
        return new LoanPartExportDto(
                name,
                startDate,
                endDate,
                amount,
                currenciesById.get(currencyId).getCurrentIsin(),
                loanType,
                interests.stream().map(LoanInterest::toExportDto).toList());
    }
}
