package org.tikito.entity.loan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.loan.LoanInterestDto;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private double amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_part_id")
    private LoanPart loanPart;

    public LoanInterest(final LocalDate startDate, final LocalDate endDate, final double amount) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
    }

    public LoanInterestDto toDto() {
        return new LoanInterestDto(
                id,
                startDate,
                endDate,
                amount);
    }

}
