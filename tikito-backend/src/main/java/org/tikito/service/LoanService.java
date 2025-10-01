package org.tikito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.controller.request.CreateOrUpdateLoanPartRequest;
import org.tikito.controller.request.CreateOrUpdateLoanRequest;
import org.tikito.dto.loan.LoanDto;
import org.tikito.dto.loan.LoanPartDto;
import org.tikito.dto.loan.LoanValueDto;
import org.tikito.entity.Job;
import org.tikito.entity.loan.Loan;
import org.tikito.entity.loan.LoanInterest;
import org.tikito.entity.loan.LoanPart;
import org.tikito.entity.loan.LoanValue;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.repository.*;
import org.tikito.service.job.JobType;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final LoanPartRepository loanPartRepository;
    private final LoanInterestRepository loanInterestRepository;
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;
    private final LoanValueRepository loanValueRepository;
    private final JobService jobService;

    public LoanService(final LoanRepository loanRepository,
                       final LoanPartRepository loanPartRepository,
                       final LoanInterestRepository loanInterestRepository,
                       final MoneyTransactionGroupRepository moneyTransactionGroupRepository,
                       final LoanValueRepository loanValueRepository,
                       final JobService jobService) {
        this.loanRepository = loanRepository;
        this.loanPartRepository = loanPartRepository;
        this.loanInterestRepository = loanInterestRepository;
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
        this.loanValueRepository = loanValueRepository;
        this.jobService = jobService;
    }

    public List<LoanDto> getLoans(final long userId) {
        return loanRepository
                .findByUserId(userId)
                .stream()
                .map(Loan::toDto)
                .toList();
    }

    public LoanDto getLoan(final long userId, final long loanId) {
        return loanRepository
                .findByUserIdAndId(userId, loanId)
                .orElseThrow()
                .toDto();
    }

    public List<LoanValueDto> getLoanValues(final long userId) {
        final Set<Long> loanPartIds = loanPartRepository
                .findByUserId(userId)
                .stream()
                .map(LoanPart::getId)
                .collect(Collectors.toSet());
        return loanValueRepository
                .findByLoanPartIds(loanPartIds)
                .stream()
                .map(LoanValue::toDto)
                .toList();
    }

    public List<LoanValueDto> getCurrentLoanValues(final long userId) {
        final Set<Long> loanPartIds = loanPartRepository
                .findByUserId(userId)
                .stream()
                .map(LoanPart::getId)
                .collect(Collectors.toSet());
        final Map<Long, LoanValueDto> values = new HashMap<>();
        loanValueRepository
                .findByLoanPartIdsAndDateBefore(loanPartIds, LocalDate.now())
                .stream()
                .map(LoanValue::toDto)
                .forEach(value -> {
                    if(!values.containsKey(value.getLoanPartId())) {
                        values.put(value.getLoanPartId(), value);
                    }
                });
        return values.values().stream().toList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public LoanDto createOrUpdateLoan(final long userId, final CreateOrUpdateLoanRequest request) {
        final Loan loan = request.isNew() ? new Loan(userId) : loanRepository.findByUserIdAndId(userId, request.getId()).orElseThrow();
        final List<MoneyTransactionGroup> groups = moneyTransactionGroupRepository.findAllById(request.getGroupIds());
        if (groups.size() != request.getGroupIds().size()) {
            throw new NoSuchElementException();
        }
        loan.setName(request.getName());
        loan.setDateRange(request.getDateRange());
        loan.setGroups(groups);

        final Loan loanPersisted = loanRepository.saveAndFlush(loan);

        groups.forEach(group -> group.setLoan(loanPersisted)); // todo
        moneyTransactionGroupRepository.saveAllAndFlush(groups);
        jobService.addJob(Job.loan(JobType.RECALCULATE_LOAN, loan.getId(), userId).build());
        return loanPersisted.toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public LoanPartDto createOrUpdateLoanPart(final long userId, final CreateOrUpdateLoanPartRequest request) {
        final Loan loan = loanRepository.findByUserIdAndId(userId, request.getLoanId()).orElseThrow();
        final LoanPart loanPart = request.isNew() ? new LoanPart(userId, loan) : loanPartRepository.findByUserIdAndId(userId, request.getId()).orElseThrow();
        final Map<Long, LoanInterest> existingQualifiersMap = loanPart.getInterests().stream().collect(Collectors.toMap(LoanInterest::getId, Function.identity()));

        loanPart.setName(request.getName());
        loanPart.setStartDate(request.getStartDate());
        loanPart.setEndDate(request.getEndDate());
        loanPart.setAmount(request.getAmount());
        loanPart.setRemainingAmount(request.getAmount());
        loanPart.setLoanType(request.getLoanType());
        loanPart.setCurrencyId(request.getCurrencyId());
        loanPart.getInterests().clear();

        if (request.getInterests() != null) {
            request
                    .getInterests()
                    .stream()
                    .map(interest -> new LoanInterest(
                            existingQualifiersMap.containsKey(interest.getId()) ? interest.getId() : null,
                            interest.getStartDate() == null ? loanPart.getStartDate() : interest.getStartDate(),
                            interest.getEndDate(),
                            interest.getAmount(),
                            loanPart))
                    .forEach(interest -> loanPart.getInterests().add(interest));
        }

        // todo: what about the unselected groups. We need to set the loan_id to null
        final LoanPart entity = loanPartRepository.saveAndFlush(loanPart);
        jobService.addJob(Job.loan(JobType.RECALCULATE_LOAN, loan.getId(), userId).build());
        return entity.toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteLoan(final long userId, final long loanId) {
        loanRepository.findByUserIdAndId(userId, loanId).orElseThrow();
        loanRepository.deleteByUserIdAndId(userId, loanId);
        loanValueRepository.deleteByLoanId(loanId);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteLoanPart(final long userId, final long loanId, final long loanPartId) {
        loanPartRepository.findByUserIdAndId(userId, loanPartId).orElseThrow();
        loanPartRepository.deleteByUserIdAndId(userId, loanPartId);
        loanValueRepository.deleteByLoanPartId(loanPartId);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteLoanInterest(final long userId, final long loanId, final long loanPartId, final long loanInterestId) {
        final LoanPart loanPart = loanPartRepository.findByUserIdAndId(userId, loanPartId).orElseThrow();
        final LoanInterest interest = loanPart.getInterests().stream().filter(i -> i.getId() == loanInterestId).findFirst().orElseThrow();
        interest.setLoanPart(null);
        loanPart.getInterests().remove(interest);
        loanPartRepository.saveAndFlush(loanPart);
        loanValueRepository.deleteByLoanPartId(loanPartId);
    }
}
