package org.tikito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.DateRange;
import org.tikito.dto.loan.LoanType;
import org.tikito.dto.money.MoneyTransactionGroupQualifierType;
import org.tikito.dto.money.MoneyTransactionGroupType;
import org.tikito.entity.loan.Loan;
import org.tikito.entity.loan.LoanInterest;
import org.tikito.entity.loan.LoanPart;
import org.tikito.entity.loan.LoanValue;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.repository.LoanValueRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class LoanValueServiceTest extends BaseIntegrationTest {

    @Autowired
    private LoanValueService service;

    @Autowired
    private LoanValueRepository loanValueRepository;

    private MoneyTransactionGroup transactionGroup;

    @BeforeEach
    void setup() {
        withDefaultCurrencies();
        withDefaultUserAccount();
        withDefaultAccounts();
        withDefaultMoneyTransactionGroups();
        loginWithDefaultUser();
        transactionGroup = withExistingTransactionGroup(DEFAULT_ACCOUNT.getId(), "Mortgage", MoneyTransactionGroupQualifierType.INCLUDES, "Mortgage", Set.of(MoneyTransactionGroupType.LOAN));
    }

    @Test
    void generateLoanValues_given_loanWithNoInterest_and_exactPayments() {
        final Loan loan = generateLoan(DateRange.DAY, 0, -1);

        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -11);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 16), -11);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 17), -11);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22, 11, 11, 0, 0, 0, values.get(0));
        assertLoanValue(11, 22, 11, 0, 0, 0, values.get(1));
        assertLoanValue(0, 33, 11, 0, 0, 0, values.get(2));
    }

    @Test
    void generateLoanValues_given_loanWithNoInterest_and_noExactPayments_fullyPaidFinally() {
        final LocalDate startDate = LocalDate.of(2025, 4, 15);
        final Loan loan = generateLoan(
                DateRange.DAY,
                startDate, startDate.plusDays(3),
                33,
                0);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -11);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 16), -10);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 17), -12);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22, 11, 11, 0, 0, 0, values.get(0));
        assertLoanValue(12, 21, 10, 0, 0, 0, values.get(1));
        assertLoanValue(0, 33, 12, 0, 0, 0, values.get(2));
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_exactPayments() {
        final LocalDate startDate = LocalDate.of(2025, 4, 15);
        final Loan loan = generateLoan(
                DateRange.MONTH,
                startDate, startDate.plusMonths(3),
                33,
                50);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -11.92913);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 5, 15), -11.92913);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 6, 15), -11.92914);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22.44587, 10.55413, 10.55413, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(11.45198, 21.54802, 10.99389, 0, 2.31024, 0.93524, values.get(1));
        assertLoanValue(0, 33, 11.45197, 0, 2.78741, 0.47717, values.get(2));
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_exactPayments_and_multipleParts() {
        final Loan loan = generateLoan(DateRange.MONTH, 50, 70);

        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -34.306542695);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 5, 15), -34.306542695);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 6, 15), -34.306542695);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22.44587, 10.55413, 10.55413, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(41.12259, 18.87741, 18.87741, 0, 3.5, 3.5, values.get(1));
        assertLoanValue(11.45198, 21.54802, 10.99389, 0, 2.31024, 0.93524, values.get(2));
        assertLoanValue(21.14401, 38.85599, 19.97859, 0, 5.89882, 2.39882, values.get(3));
        assertLoanValue(0, 33, 11.45197, 0, 2.78741, 0.47717, values.get(4));
        assertLoanValue(0, 60, 21.14401, 0, 7.13222, 1.2334, values.get(5));
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_noExactPayments_fullyPaidFinally() {
        final Loan loan = generateLoan(DateRange.MONTH, 50, -1);

        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -11.929136590782884);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 5, 15), -11);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 6, 15), -12.89699);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22.4459, 10.55414, 10.55414, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(12.38111, 20.61889, 10.06476, 0, 2.31024, 0.93524, values.get(1));
        assertLoanValue(0, 33, 12.38111, 0, 2.82612, 0.51588, values.get(2));
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_noExactPayments_fullyPaidFinally_and_multipleParts() {
        final Loan loan = generateLoan(DateRange.MONTH, 50, 70);

        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -34.306542695);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 5, 15), -20);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 6, 15), -49.447633714);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22.4459, 10.55414, 10.55414, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(41.12259, 18.87741, 18.87741, 0, 3.5, 3.5, values.get(1));
        assertLoanValue(11.45198, 21.54802, 10.99389, 0, 2.31024, 0.93524, values.get(2));
        assertLoanValue(35.45055, 24.54945, 5.67205, 0, 5.89882, 2.39882, values.get(3));
        assertLoanValue(0, 33, 11.45197, 0, 2.78741, 0.47717, values.get(4));
        assertLoanValue(0, 60, 35.45055, 0, 7.96677, 2.06795, values.get(5));
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_noExactPaymentsPaidMore_fullyPaidFinally() {
        final Loan loan = generateLoan(DateRange.MONTH, 50, -1);

        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -11.929136590782884);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 5, 15), -15);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 6, 15), -8.73032053951);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22.4459, 10.55414, 10.55414, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(8.38111, 24.61889, 14.06476, 0, 2.31024, 0.93524, values.get(1));
        assertLoanValue(0, 33, 8.38111, 0, 2.65946, 0.34921, values.get(2));
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_noExactPaymentsPaidMore_fullyPaidFinally_multipleParts() {
        final Loan loan = generateLoan(DateRange.MONTH, 50, 70);

        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -34.306542695);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 5, 15), -60);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 6, 15), -7.305166567);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22.4459, 10.55414, 10.55414, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(41.12259, 18.87741, 18.87741, 0, 3.5, 3.5, values.get(1));
        assertLoanValue(0, 33, 22.44586, 0, 2.31024, 0.93524, values.get(2));
        assertLoanValue(6.90252, 53.09748, 34.22007, 0, 5.89882, 2.3988, values.get(3));
        assertLoanValue(0, 33, 0, 0, 2.31024, 0, values.get(4));
        assertLoanValue(0, 60, 6.90252, 0, 6.30146, 0.40265, values.get(5));

    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_oneMissingPayment_notFullyPaid() {
        final Loan loan = generateLoan(DateRange.MONTH, 50, -1);

        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -11);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 6, 15), -12);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(23.375, 9.625, 9.625, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(23.375, 9.625, 0, 0.97396, 1.375, 0, values.get(1));
        assertLoanValue(13.32292, 19.67708, 10.05208, 0, 3.32292, 1.94792, values.get(2));
    }

    @Test
    void generateLoanValues_given_loanWithZeroInterest_and_oneMissingPayment_notFullyPaid_simulated() {
        final LocalDate startDate = LocalDate.now().minusMonths(1);
        final Loan loan = generateLoan(
                DateRange.MONTH,
                startDate, startDate.plusMonths(3),
                33,
                0);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), startDate, -11);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22, 11, 11, 0, 0, 0, values.get(0));
        assertLoanValue(11, 22, 11, 0, 0, 0, values.get(1));
        assertLoanValue(0, 33, 11, 0, 0, 0, values.get(2));

        assertFalse(values.get(0).isSimulated());
        assertTrue(values.get(1).isSimulated());
        assertTrue(values.get(2).isSimulated());
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_oneMissingPayment_notFullyPaid_simulated() {
        final LocalDate startDate = LocalDate.now().minusMonths(1);
        final Loan loan = generateLoan(
                DateRange.MONTH,
                startDate, startDate.plusMonths(3),
                33,
                50);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), startDate, -11.92913);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        // exact same as a not simulated exact payment
        assertLoanValue(22.44587, 10.55413, 10.55413, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(11.45198, 21.54802, 10.99389, 0, 2.31024, 0.93524, values.get(1));
        assertLoanValue(0, 33, 11.45197, 0, 2.78741, 0.47717, values.get(2));

        assertFalse(values.get(0).isSimulated());
        assertTrue(values.get(1).isSimulated());
        assertTrue(values.get(2).isSimulated());
    }

    @Test
    void generateLoanValues_given_loanWithNoInterest_and_exactPayments_and_multipleParts() {
        final Loan loan = generateLoan(DateRange.DAY, 0, 0);

        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -31);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 16), -31);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 17), -31);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22, 11, 11, 0, 0, 0, values.get(0));
        assertLoanValue(40, 20, 20, 0, 0, 0, values.get(1));
        assertLoanValue(11, 22, 11, 0, 0, 0, values.get(2));
        assertLoanValue(20, 40, 20, 0, 0, 0, values.get(3));
        assertLoanValue(0, 33, 11, 0, 0, 0, values.get(4));
        assertLoanValue(0, 60, 20, 0, 0, 0, values.get(5));
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_oneMissingPayment_and_multipleParts_notFullyPaid() {
        final Loan loan = generateLoan(DateRange.MONTH, 50, 70);


        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 4, 15), -34.306542695);
        withExistingMortgageTransaction(loan.getId(), transactionGroup.getId(), LocalDate.of(2025, 6, 15), -34.306542695);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        assertLoanValue(22.44587, 10.55413, 10.55413, 0, 1.375, 1.375, values.get(0));
        assertLoanValue(41.12259, 18.87741, 18.87741, 0, 3.5, 3.5, values.get(1));
        assertLoanValue(22.44586, 10.55414, 0, 0.9352, 1.375, 0, values.get(2));
        assertLoanValue(41.12259, 18.87741, 0, 2.39882, 3.5, 0, values.get(3));
        assertLoanValue(0, 33.0, 22.44586, 0, 3.24549, 1.87049, values.get(4));
        assertLoanValue(35.93004, 24.06996, 5.19255, 0, 8.29764, 4.79764, values.get(5));
    }

    @Test
    void generateLoanValues_given_loanWithSingleInterest_and_startsHalfPeriod_simulated() {
        final int year = LocalDate.now().getYear() + 1;
        final LocalDate startDate = LocalDate.of(year, 3, 25);
        final Loan loan = generateLoan(
                DateRange.MONTH,
                startDate, LocalDate.of(year, 6, 30),
                33,
                0);

        service.generateLoanValues(DEFAULT_USER_ACCOUNT.getId(), loan.getId());
        final List<LoanValue> values = loanValueRepository.findAll();

        int i = 5;
    }

    private void assertLoanValue(final double amountRemaining, final double loanPaid, final double loanPaidThisPeriod, final double interestRemaining, final double interestPaid, final double interestPaidThisPeriod, final LoanValue value) {

        assertDoubleEquals(amountRemaining, value.getAmountRemaining(), 4);
        assertDoubleEquals(loanPaid, value.getLoanPaid(), 4);
        assertDoubleEquals(loanPaidThisPeriod, value.getLoanPaidThisPeriod(), 4);
        assertDoubleEquals(interestRemaining, value.getInterestRemaining(), 4);
        assertDoubleEquals(interestPaid, value.getInterestPaid(), 4);
        assertDoubleEquals(interestPaidThisPeriod, value.getInterestPaidThisPeriod(), 4);
    }

    private Loan generateLoan(final DateRange dateRange, final int interest, final int secondInterest) {
        final LocalDate startDate = LocalDate.of(2025, 4, 15);
        final LocalDate endDate = dateRange == DateRange.MONTH ? startDate.plusMonths(3) : startDate.plusDays(3);
        final Loan loan = generateLoan(
                dateRange,
                startDate, endDate,
                33,
                interest);
        if (secondInterest >= 0) {
            final LoanInterest secondPartInterest = loanInterest(secondInterest, startDate, endDate);
            final LoanPart secondPart = loanPart("Part 2",
                    60,
                    0,
                    startDate, endDate,
                    LoanType.MORTGAGE_ANNUITEIT,
                    new ArrayList<>(List.of(
                            secondPartInterest)));
            secondPartInterest.setLoanPart(secondPart);
            secondPart.setLoan(loan);
            loan.getLoanParts().add(secondPart);
            return loanRepository.saveAndFlush(loan);
        }
        return loan;
    }

    private Loan generateLoan(final DateRange dateRange, final LocalDate startDate, final LocalDate endDate, final int amount, final double interest) {
        return withExistingLoan(dateRange, randomString(5), new ArrayList<>(List.of(
                loanPart("Part 1",
                        amount,
                        0,
                        startDate,
                        endDate,
                        LoanType.MORTGAGE_ANNUITEIT,
                        new ArrayList<>(List.of(
                                loanInterest(interest, startDate, endDate)))))));
    }
}