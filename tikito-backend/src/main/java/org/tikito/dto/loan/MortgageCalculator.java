package org.tikito.dto.loan;

import lombok.Getter;
import org.tikito.dto.DateRange;
import org.tikito.entity.loan.LoanInterest;
import org.tikito.entity.loan.LoanPart;
import org.tikito.util.Util;

public class MortgageCalculator extends LoanValueCalculator {

    public static double calculateAnnuiteit(final double totalAmount, final double interestPerMonthMultiplier, final long amountOfPeriodsInDateRange) {
        if(interestPerMonthMultiplier == 0) {
            // no interest
            return totalAmount / amountOfPeriodsInDateRange;
        }

        return totalAmount *
                interestPerMonthMultiplier /
                (1 - Math.pow(1 + interestPerMonthMultiplier, -amountOfPeriodsInDateRange));
    }


    /**
     * Returns the annuiteit (repayment + interest) to be paid. The interest is calculated over the remaining amount of
     * the loan. Although we initially calculate the annuiteit here based on the standard formula, we later add the
     * remaining amount not yet paid to the repayment. So the final annuiteit = repayment + interest + remaining_not_yet_paid_repayment.
     */
    public static RangedPayment calculateMonthlyTotalPaymentAmount(final DateRange dateRange, final LoanPart loanPart, final LoanInterest interest, final double remainingAmountNotYetPaid) {
        return switch (loanPart.getLoanType()) {
            case GENERIC -> null;
            case MORTGAGE_ANNUITEIT -> calculateMonthlyTotalPaymentAmountAnnuity(dateRange, loanPart, interest, remainingAmountNotYetPaid);
            case MORTGAGE_LINEAR -> calculateMonthlyTotalPaymentAmountLinear(dateRange, loanPart, interest);
            case STUDENT -> null;
        };
    }

    public static RangedPayment calculateMonthlyTotalPaymentAmountLinear(final DateRange dateRange, final LoanPart loanPart, final LoanInterest interest) {
        final int dateRangeAmountPerPeriod = getDateRangeAmountPerPeriod(dateRange);
        final long amountOfPeriodsInDateRange = Util.getChronoUnit(dateRange).between(interest.getStartDate(), loanPart.getEndDate());
        final double repayment = loanPart.getAmount() / amountOfPeriodsInDateRange;
        final double interestPerDateRangeMultiplier = interest.getAmount() / 100 / dateRangeAmountPerPeriod;
        final double interestPaid = loanPart.getRemainingAmount() * interestPerDateRangeMultiplier;
        return new RangedPayment(repayment, interestPaid);
    }

    public static RangedPayment calculateMonthlyTotalPaymentAmountAnnuity(final DateRange dateRange, final LoanPart loanPart, final LoanInterest interest, final double remainingAmountNotYetPaid) {
        // todo: what if the interest changes over time. Does the annuiteit also changes?
        // duration is from the start of the interest, until to end of the loan
        final int dateRangeAmountPerPeriod = getDateRangeAmountPerPeriod(dateRange);
        final long amountOfPeriodsInDateRange = Util.getChronoUnit(dateRange).between(interest.getStartDate(), loanPart.getEndDate());
        final double interestPerDateRangeMultiplier = interest.getAmount() / 100 / dateRangeAmountPerPeriod;
        final double annuiteit = calculateAnnuiteit(loanPart.getAmount(), interestPerDateRangeMultiplier, amountOfPeriodsInDateRange);
        final double interestPaid = loanPart.getRemainingAmount() * interestPerDateRangeMultiplier;
        final double repayment = annuiteit - interestPaid + remainingAmountNotYetPaid;
        return new RangedPayment(repayment, interestPaid);
    }

    public static int getDateRangeAmountPerPeriod(final DateRange dateRange) {
        return switch (dateRange) {
            case YEAR -> 1;
            case MONTH -> 12;
            case WEEK -> 52; // todo, not always 52
            case DAY, ALL -> 365; // todo, not always 365
        };
    }

    @Getter
    public static class RangedPayment {
        private double repayment;
        private double interest;
        private double total;

        public RangedPayment(final double repayment, final double interest) {
            this.repayment = repayment;
            this.interest = interest;
            this.total = repayment + interest;
        }

        public void addToInterest(final double amount) {
            this.interest += amount;
            total = repayment + interest;
        }

        public void addToRepayment(final double amount) {
            this.repayment += amount;
            total = repayment + interest;
        }
    }
}
