package org.tikito.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.DateRange;
import org.tikito.dto.loan.AnnuiteitMortgageCalculator;
import org.tikito.dto.money.MoneyTransactionGroupType;
import org.tikito.entity.Job;
import org.tikito.entity.loan.Loan;
import org.tikito.entity.loan.LoanInterest;
import org.tikito.entity.loan.LoanPart;
import org.tikito.entity.loan.LoanValue;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.repository.LoanRepository;
import org.tikito.repository.LoanValueRepository;
import org.tikito.repository.MoneyTransactionGroupRepository;
import org.tikito.repository.MoneyTransactionRepository;
import org.tikito.service.job.JobProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import static org.tikito.service.job.JobType.RECALCULATE_LOAN;

@Service
@Slf4j
public class LoanValueService implements JobProcessor {
    private final LoanRepository loanRepository;
    private final LoanValueRepository loanValueRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;

    public LoanValueService(final LoanRepository loanRepository,
                            final LoanValueRepository loanValueRepository,
                            final MoneyTransactionRepository moneyTransactionRepository,
                            final MoneyTransactionGroupRepository moneyTransactionGroupRepository) {
        this.loanRepository = loanRepository;
        this.loanValueRepository = loanValueRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
    }

    @Transactional//(propagation = Propagation.MANDATORY)
    public void generateLoanValues(final long userId, final long loanId) {
        final Loan loan = loanRepository.findByUserIdAndId(userId, loanId).orElseThrow();

        final List<MoneyTransaction> transactions = getMoneyTransactions();

        generateLoanPartValues(loan, loan.getDateRange(), transactions);
    }

    private List<MoneyTransaction> getMoneyTransactions() {
        final Set<Long> moneyGroupIds = moneyTransactionGroupRepository
                .findAll()
                .stream()
                .filter(group -> group.getGroupTypes().contains(MoneyTransactionGroupType.LOAN))
                .map(MoneyTransactionGroup::getId)
                .collect(Collectors.toSet());
        return moneyTransactionRepository
                .findByGroupIdIn(moneyGroupIds)
                .stream()
                .sorted((Comparator<MoneyTransaction>) (transaction, t1) -> transaction.getTimestamp().compareTo(t1.getTimestamp()))
                .toList();
    }

    /**
     * For all the loan parts we generate loan values per date range. Then, we loop over the transactions and subtract
     * them from the current loan part. If someone paid too little for the parts of this date range, we just continue
     * to find newer transactions.
     * <p>
     * In case someone paid too much, we will subtract the amount that is paid too much from the loan parts of the next
     * date range.
     */
    private void generateLoanPartValues(final Loan loan, final DateRange dateRange, final List<MoneyTransaction> transactions) {
        final LocalDate startDate = getStartDate(loan);
        final LocalDate endDate = getEndDate(loan);
        final Map<Long, LoanValue> previousValuesPerLoanPartId = getInitialValues(loan, startDate);
        final List<LoanValue> values = new ArrayList<>();
        final LocalDate now = LocalDate.now();

        LinkedValue currentLinkedValue = generateLinkedValues(startDate, endDate, dateRange, loan.getLoanParts());
        LinkedTransactionValue currentTransactionValue = generateLinkedMoneyValues(transactions);

        if (startDate == null) {
            log.warn("No start date found in parts for loan {}", loan.getId());
            return;
        }

        // this is the amount that someone could be behind on interest
        final Map<Long, Double> remainingInterestNotYetPaidPerPart = loan.getLoanParts().stream().collect(Collectors.toMap(LoanPart::getId, (ignore) -> 0.0));
        // this is the amount that someone should have repaid each period in theory
        final Map<Long, Double> totalRepaymentShouldBeDonePerPart = loan.getLoanParts().stream().collect(Collectors.toMap(LoanPart::getId, (ignore) -> 0.0));

        while (currentLinkedValue != null) {
            double amountPaidThisPeriod = 0;

            // now we fetch the total amount paid this period
            while (currentTransactionValue != null) {
                final LocalDate transactionDate = LocalDate.ofInstant(currentTransactionValue.getTransaction().getTimestamp(), ZoneOffset.systemDefault());
                final boolean transactionAfterCurrentDate = transactionDate.isAfter(currentLinkedValue.getDate());
                if (transactionAfterCurrentDate) {
                    break;
                } else {
                    amountPaidThisPeriod += -currentTransactionValue.getTransaction().getAmount();
                    currentTransactionValue = currentTransactionValue.getNext();
                }
            }

            // we simulate when there is no interest left to pay and its in the future
            final boolean simulate = currentLinkedValue.getDate().isAfter(now);

            for (final LoanPart loanPart : currentLinkedValue.getLoanParts()) {
                final LoanInterest interest = getInterestOrDefault(currentLinkedValue.getDate(), loanPart);
                final LoanValue value = new LoanValue(previousValuesPerLoanPartId.get(loanPart.getId()), currentLinkedValue.getDate());
                final AnnuiteitMortgageCalculator.RangedPayment monthlyPayment = AnnuiteitMortgageCalculator.calculateMonthlyTotalPaymentAmount(dateRange, loanPart, interest, totalRepaymentShouldBeDonePerPart.get(loanPart.getId()) - value.getLoanPaid());

                // already paid everything?
                if (value.getAmountRemaining() <= 0) {
                    value.setAmountRemaining(0);
                    value.setLoanPaidThisPeriod(0);
                    value.setInterestPaidThisPeriod(0);
                    value.setLoanPaid(loanPart.getAmount());
                    values.add(value);
                    previousValuesPerLoanPartId.put(loanPart.getId(), value);
                    continue;
                }

                // we do min(), because if we paid too much for one month, we don't need to pay the full repayment this month.
                totalRepaymentShouldBeDonePerPart.compute(loanPart.getId(), (partId, v) -> Math.min(loanPart.getAmount(), v + monthlyPayment.getRepayment()));
                remainingInterestNotYetPaidPerPart.compute(loanPart.getId(), (partId, v) -> v + monthlyPayment.getInterest());

                final double remainingInterestNotYetPaidForThisPart = remainingInterestNotYetPaidPerPart.get(loanPart.getId());
                final double interestPaidThisPeriod = Math.min(remainingInterestNotYetPaidForThisPart, amountPaidThisPeriod);
                final double totalToBePaidThisPeriod = totalRepaymentShouldBeDonePerPart.get(loanPart.getId()) - value.getLoanPaid();

                if (!simulate) {
                    amountPaidThisPeriod = processPartPayment(amountPaidThisPeriod, interestPaidThisPeriod, value, loanPart, remainingInterestNotYetPaidPerPart, totalToBePaidThisPeriod);
                } else {
                    processSimulatedPartPayment(totalToBePaidThisPeriod, value, remainingInterestNotYetPaidForThisPart, loanPart, remainingInterestNotYetPaidPerPart);
                }

                value.setRepaymentRemaining(totalRepaymentShouldBeDonePerPart.get(loanPart.getId()) - value.getLoanPaid());
                values.add(value);
                previousValuesPerLoanPartId.put(loanPart.getId(), value);
                loanPart.setRemainingAmount(value.getAmountRemaining());
            }

            // in case we paid more than required
            if (amountPaidThisPeriod > 0) {
                processMoreThanUsualPaid(currentLinkedValue, amountPaidThisPeriod, previousValuesPerLoanPartId, totalRepaymentShouldBeDonePerPart);
            }

            currentLinkedValue = currentLinkedValue.getNext();
        }

        final List<LoanValue> list = values.stream().filter(this::notNan).map(this::round).toList();
        loanValueRepository.deleteByLoanPartIdIn(loan.getLoanParts().stream().map(LoanPart::getId).collect(Collectors.toSet()));
        loanValueRepository.saveAllAndFlush(list);
        loanRepository.saveAndFlush(loan);

        log.info("Persisted {} values", list.size());
    }

    private double processPartPayment(double amountPaidThisPeriod,
                                      final double interestPaidThisPeriod,
                                      final LoanValue value,
                                      final LoanPart loanPart,
                                      final Map<Long, Double> remainingInterestNotYetPaidPerPart,
                                      final double totalToBePaidThisPeriod) {
        // calculate interest paid this month. First deduct the remaining interest left
        amountPaidThisPeriod -= interestPaidThisPeriod;
        final double remainingInterestNotYetPaidForThisPart = remainingInterestNotYetPaidPerPart.compute(loanPart.getId(), (partId, v) -> v - interestPaidThisPeriod);
        value.setInterestPaidThisPeriod(interestPaidThisPeriod);
        value.setInterestPaid(value.getInterestPaid() + interestPaidThisPeriod);
        value.setInterestRemaining(remainingInterestNotYetPaidForThisPart);

        if (amountPaidThisPeriod > 0) {
            // there is something left, lets repay the loan a bit. We try to pay as much as possible what we
            // have not paid yet, including payments that are behind.
            final double repaymentPaidThisMonth = Math.min(totalToBePaidThisPeriod, amountPaidThisPeriod);

            value.setLoanPaidThisPeriod(repaymentPaidThisMonth);
            value.setLoanPaid(value.getLoanPaid() + repaymentPaidThisMonth);
            value.setAmountRemaining(value.getAmountRemaining() - repaymentPaidThisMonth);
            amountPaidThisPeriod -= repaymentPaidThisMonth;
        }

        return amountPaidThisPeriod;
    }

    private void processSimulatedPartPayment(final double totalToBePaidThisPeriod, final LoanValue value, final double remainingInterestNotYetPaidForThisPart, final LoanPart loanPart, final Map<Long, Double> remainingInterestNotYetPaidPerPart) {
        /*
        calculate what we should pay this month:
            a:  repayment that should be done. This is the amount of money that should have been paid
                up until now, if all payments were done. We subtract the remaining part to see how
                far we are behind paying.
            b: the complete remaining amount, in case there are more periods to be simulated
         */
        final double repaymentDoneThisPeriod = Math.min(totalToBePaidThisPeriod, value.getAmountRemaining());

        value.setSimulated(true);
        value.setInterestPaid(value.getInterestPaid() + remainingInterestNotYetPaidForThisPart);
        value.setInterestPaidThisPeriod(remainingInterestNotYetPaidForThisPart);
        value.setLoanPaid(value.getLoanPaid() + repaymentDoneThisPeriod);
        value.setLoanPaidThisPeriod(repaymentDoneThisPeriod);
        value.setAmountRemaining(value.getAmountRemaining() - repaymentDoneThisPeriod);

        value.setInterestRemaining(0);
        remainingInterestNotYetPaidPerPart.put(loanPart.getId(), 0.0);
    }

    private void processMoreThanUsualPaid(final LinkedValue currentLinkedValue, double amountPaidThisPeriod, final Map<Long, LoanValue> previousValuesPerLoanPartId, final Map<Long, Double> totalRepaymentShouldBeDonePerPart) {
        // If we don't sort on the amount, it could be that we first process a part with a large remaining amount,
        // but would not assign the larger than average part to it. This way, we end up trying to divide most of
        // the remaining loan. Therefor, we sort with the least amount going first
        final List<LoanPart> sortedParts = currentLinkedValue
                .getLoanParts()
                .stream()
                .filter(part -> part.getRemainingAmount() > 0)
                .sorted(Comparator.comparingDouble(LoanPart::getRemainingAmount))
                .toList();
        for (int i = 0; i < sortedParts.size(); i++) {
            final LoanPart loanPart = sortedParts.get(i);
            final double averageRemainderPerPart = amountPaidThisPeriod / (sortedParts.size() - i);
            final double repaymentForThisPart = Math.min(loanPart.getRemainingAmount(), averageRemainderPerPart);
            final LoanValue value = previousValuesPerLoanPartId.get(loanPart.getId());

            value.setLoanPaid(value.getLoanPaid() + repaymentForThisPart);
            value.setLoanPaidThisPeriod(value.getLoanPaidThisPeriod() + repaymentForThisPart);
            value.setAmountRemaining(value.getAmountRemaining() - repaymentForThisPart);
            loanPart.setRemainingAmount(loanPart.getRemainingAmount() - repaymentForThisPart);
            amountPaidThisPeriod -= repaymentForThisPart;
            totalRepaymentShouldBeDonePerPart.compute(loanPart.getId(), (partId, v) -> v + repaymentForThisPart);
        }
    }

    private LoanValue round(final LoanValue value) {
        value.setLoanPaid(new BigDecimal(value.getLoanPaid()).setScale(5, RoundingMode.HALF_EVEN).doubleValue());
        value.setLoanPaidThisPeriod(new BigDecimal(value.getLoanPaidThisPeriod()).setScale(5, RoundingMode.HALF_EVEN).doubleValue());
        value.setAmountRemaining(new BigDecimal(value.getAmountRemaining()).setScale(5, RoundingMode.HALF_EVEN).doubleValue());
        value.setInterestPaid(new BigDecimal(value.getInterestPaid()).setScale(5, RoundingMode.HALF_EVEN).doubleValue());
        value.setInterestPaidThisPeriod(new BigDecimal(value.getInterestPaidThisPeriod()).setScale(5, RoundingMode.HALF_EVEN).doubleValue());
        value.setInterestRemaining(new BigDecimal(value.getInterestRemaining()).setScale(5, RoundingMode.HALF_EVEN).doubleValue());
        return value;
    }

    private boolean notNan(final LoanValue value) {
        return !Double.isNaN(value.getAmountRemaining()) &&
                !Double.isNaN(value.getInterestPaidThisPeriod()) &&
                !Double.isNaN(value.getLoanPaid()) &&
                !Double.isNaN(value.getLoanPaidThisPeriod()) &&
                !Double.isNaN(value.getInterestPaid());

    }

    private LoanInterest getInterestOrDefault(final LocalDate date, final LoanPart loanPart) {
        try {
            return getInterest(date, loanPart);
        } catch (final NoSuchElementException e) {
            return new LoanInterest(loanPart.getStartDate(), date, 0);
        }
    }

    /**
     * Generates a linked list of transactions
     */
    private LinkedTransactionValue generateLinkedMoneyValues(final List<MoneyTransaction> transactions) {
        LinkedTransactionValue first = null;
        LinkedTransactionValue last = null;
        for (final MoneyTransaction transaction : transactions) {
            final LinkedTransactionValue value = new LinkedTransactionValue(transaction);
            if (first == null) {
                first = value;
                last = value;
                value.setPrevious(value);
            } else {
                last.setNext(value);
                value.setPrevious(last);
                last = value;
            }
        }
        return first;
    }

    /**
     * Generates a linked list for every day, where each entry (day) contains a list of pars that are active on that day.
     */
    private LinkedValue generateLinkedValues(final LocalDate startDate, final LocalDate endDate, final DateRange dateRange, final List<LoanPart> loanParts) {
        LinkedValue previousLinkedValue = null;
        LinkedValue first = null;
        LocalDate previousDate = startDate;
        String dateRangedString = getCurrentDateRangeString(dateRange, startDate);

        for (LocalDate currentDate = startDate;
             currentDate.isBefore(endDate.plusDays(1));
             currentDate = currentDate.plusDays(1)) {
            final String currentDateRangeString = getCurrentDateRangeString(dateRange, currentDate);

            if (!dateRangedString.equals(currentDateRangeString)) {
                final LinkedValue newLinkedValue = new LinkedValue(previousDate, getLoanPartsOnDate(loanParts, previousDate));

                if (previousLinkedValue == null) {
                    previousLinkedValue = newLinkedValue;
                    first = newLinkedValue;
                } else {
                    previousLinkedValue.setNext(newLinkedValue);
                    newLinkedValue.setPrevious(previousLinkedValue);
                    previousLinkedValue = newLinkedValue;
                }
            }

            dateRangedString = currentDateRangeString;
            previousDate = currentDate;
        }
        return first;
    }

    private List<LoanPart> getLoanPartsOnDate(final List<LoanPart> loanParts, final LocalDate date) {
        return loanParts
                .stream()
                .filter(loanPart -> !loanPart.getStartDate().isAfter(date))
                .toList();
    }

    private Map<Long, LoanValue> getInitialValues(final Loan loan, final LocalDate startDate) {
        final Map<Long, LoanValue> values = new HashMap<>();
        loan.getLoanParts().forEach(loanPart -> {
            final LoanValue value = new LoanValue();
            value.setLoanId(loanPart.getLoan().getId());
            value.setLoanPartId(loanPart.getId());
            value.setDate(startDate);
            value.setAmountRemaining(loanPart.getAmount());
            values.put(loanPart.getId(), value);
        });
        return values;
    }

    private LocalDate getStartDate(final Loan loan) {
        return loan
                .getLoanParts()
                .stream()
                .map(LoanPart::getStartDate)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private LocalDate getEndDate(final Loan loan) {
        return loan
                .getLoanParts()
                .stream()
                .map(LoanPart::getEndDate)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private LoanInterest getInterest(final LocalDate date, final LoanPart loanPart) {
        return loanPart
                .getInterests()
                .stream()
                .filter(interest ->
                        !interest.getStartDate().isAfter(date) &&
                                !interest.getEndDate().isBefore(date))
                .findFirst()
                .orElseThrow();
    }

    private String getCurrentDateRangeString(final DateRange dateRange, final LocalDate currentDate) {
        return switch (dateRange) {
            case YEAR -> Integer.toString(currentDate.getYear());
            case MONTH -> currentDate.getYear() + "-" + currentDate.getMonthValue();
            case WEEK -> currentDate.getYear() + "-" + currentDate.get(WeekFields.of(Locale.getDefault()).weekOfYear());
            case DAY, ALL ->
                    currentDate.getYear() + "-" + currentDate.getMonthValue() + "-" + currentDate.getDayOfMonth();
        };
    }

    @Override
    public boolean canProcess(final Job job) {
        return job.getJobType() == RECALCULATE_LOAN;
    }

    @Override
    public void process(final Job job) {
        generateLoanValues(job.getUserId(), job.getLoanId());
    }

    @Getter
    @Setter
    static
    class LinkedValue {
        private LinkedValue next;
        private LinkedValue previous;
        private LocalDate date;
        private List<LoanPart> loanParts;

        LinkedValue(final LocalDate date, final List<LoanPart> loanParts) {
            this.date = date;
            this.loanParts = loanParts;
        }
    }

    @Getter
    @Setter
    static
    class LinkedTransactionValue {
        private LinkedTransactionValue next;
        private LinkedTransactionValue previous;
        private MoneyTransaction transaction;

        LinkedTransactionValue(final MoneyTransaction transaction) {
            this.transaction = transaction;
        }
    }
}