package org.tikito.dto.loan;

import org.junit.jupiter.api.Test;
import org.tikito.dto.DateRange;
import org.tikito.entity.loan.LoanInterest;
import org.tikito.entity.loan.LoanPart;
import org.tikito.service.BaseTest;

import java.time.LocalDate;

class AnnuiteitMortgageCalculatorTest extends BaseTest {

    @Test
    void calculate_given_zeroInterest() {
        final LocalDate startDate = LocalDate.of(2025, 1, 1);
        final LoanPart loanPart = new LoanPart();
        final LoanInterest interest = new LoanInterest();

        loanPart.setStartDate(startDate);
        loanPart.setEndDate(startDate.plusDays(5));
        loanPart.setAmount(1000);
        loanPart.setRemainingAmount(1000);

        interest.setStartDate(loanPart.getStartDate());
        interest.setEndDate(loanPart.getEndDate());

        final AnnuiteitMortgageCalculator.RangedPayment payment = AnnuiteitMortgageCalculator.calculateMonthlyTotalPaymentAmount(DateRange.DAY, loanPart, interest, 0);
        assertDoubleEquals(200, payment.getTotal(), 0);
        assertDoubleEquals(200, payment.getRepayment(), 0);
        assertDoubleEquals(0, payment.getInterest(), 0);
    }

    @Test
    void calculate_given_interest() {
        final LocalDate startDate = LocalDate.of(2025, 1, 1);
        final LoanPart loanPart = new LoanPart();
        final LoanInterest interest = new LoanInterest();

        loanPart.setStartDate(startDate);
        loanPart.setEndDate(startDate.plusMonths(3));
        loanPart.setAmount(33);
        loanPart.setRemainingAmount(33);

        interest.setStartDate(loanPart.getStartDate());
        interest.setEndDate(loanPart.getEndDate());
        interest.setAmount(50);

        final AnnuiteitMortgageCalculator.RangedPayment payment = AnnuiteitMortgageCalculator.calculateMonthlyTotalPaymentAmount(DateRange.MONTH, loanPart, interest, 0);
        assertDoubleEquals(11.93, payment.getTotal(), 2);
        assertDoubleEquals(10.55, payment.getRepayment(), 2);
        assertDoubleEquals(1.375, payment.getInterest(), 3);
    }

    @Test
    void calculateMonthlyTotalPaymentAmount() {
        final LocalDate startDate = LocalDate.of(2025, 1, 1);
        final LoanInterest interest1 = new LoanInterest();
        final LoanInterest interest2 = new LoanInterest();
        final LoanPart loanPart = new LoanPart();

        loanPart.setStartDate(startDate);
        loanPart.setEndDate(startDate.plusYears(30));
        loanPart.setRemainingAmount(500000);

        interest1.setAmount(2);
        interest1.setStartDate(startDate);
        interest1.setEndDate(startDate.plusYears(5));
//
//        interest2.setAmount(4);
//        interest2.setStartDate(startDate.plusYears(5));
//        interest2.setEndDate(startDate.plusYears(10));

        final AnnuiteitMortgageCalculator.RangedPayment payment1 = AnnuiteitMortgageCalculator.calculateMonthlyTotalPaymentAmount(DateRange.MONTH, loanPart, interest1, 0);
        assertDoubleEquals(1848, payment1.getTotal(), 0);
        assertDoubleEquals(1015, payment1.getRepayment(), 0);
        assertDoubleEquals(833, payment1.getInterest(), 0);

//        loanPart.setRemainingAmount(152729);
//
//        final AnnuiteitMortgageCalculator.RangedPayment payment2 = AnnuiteitMortgageCalculator.calculateMonthlyTotalPaymentAmount(loanPart, interest1);
//        assertDoubleEquals(1931, payment2.getTotal(), 0);
//        assertDoubleEquals(1931, payment2.getRepayment(), 0);
//        assertDoubleEquals(1931, payment2.getInterest(), 0);
    }
}