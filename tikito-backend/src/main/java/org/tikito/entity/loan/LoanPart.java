package org.tikito.entity.loan;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.loan.LoanPartDto;
import org.tikito.dto.loan.LoanType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
}
