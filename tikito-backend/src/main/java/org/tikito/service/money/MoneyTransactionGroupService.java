package org.tikito.service.money;

import org.tikito.controller.request.CreateOrUpdateMoneyTransactionGroupRequest;
import org.tikito.dto.AccountDto;
import org.tikito.dto.money.HistoricalBudgetValueDto;
import org.tikito.dto.money.MoneyTransactionGroupDto;
import org.tikito.entity.Account;
import org.tikito.entity.Job;
import org.tikito.entity.money.HistoricalBudgetValue;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.money.MoneyTransactionGroupQualifier;
import org.tikito.repository.AccountRepository;
import org.tikito.repository.HistoricalBudgetValueRepository;
import org.tikito.repository.MoneyTransactionGroupRepository;
import org.tikito.repository.MoneyTransactionRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tikito.service.BudgetValueService;
import org.tikito.service.job.JobProcessor;
import org.tikito.service.job.JobType;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MoneyTransactionGroupService implements JobProcessor {
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;
    private final AccountRepository accountRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final HistoricalBudgetValueRepository historicalBudgetValueRepository;
    private final BudgetValueService budgetValueService;

    public MoneyTransactionGroupService(final MoneyTransactionGroupRepository moneyTransactionGroupRepository,
                                        final AccountRepository accountRepository,
                                        final MoneyTransactionRepository moneyTransactionRepository,
                                        final HistoricalBudgetValueRepository historicalBudgetValueRepository,
                                        final BudgetValueService budgetValueService) {
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
        this.accountRepository = accountRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.historicalBudgetValueRepository = historicalBudgetValueRepository;
        this.budgetValueService = budgetValueService;
    }

    public List<MoneyTransactionGroupDto> getGroups(final long userId) {
        return moneyTransactionGroupRepository
                .findByUserId(userId)
                .stream()
                .map(MoneyTransactionGroup::toDto)
                .sorted(Comparator.comparing(MoneyTransactionGroupDto::getName))
                .toList();
    }

    public MoneyTransactionGroupDto getGroup(final long userId, final long groupId) {
        return moneyTransactionGroupRepository
                .findByUserIdAndId(userId, groupId)
                .orElseThrow()
                .toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MoneyTransactionGroupDto createOrUpdateGroup(final long userId, final @Valid @NotNull CreateOrUpdateMoneyTransactionGroupRequest request) {
        final MoneyTransactionGroup group = request.isNew() ? new MoneyTransactionGroup(userId) : moneyTransactionGroupRepository.findByUserIdAndId(userId, request.getId()).orElseThrow();
        final Map<Long, MoneyTransactionGroupQualifier> existingQualifiersMap = group.getQualifiers().stream().collect(Collectors.toMap(MoneyTransactionGroupQualifier::getId, Function.identity()));

        group.setName(request.getName());
        group.getQualifiers().clear();
        group.setGroupTypes(request.getGroupTypes());
        group.setAccountIds(new HashSet<>(request.getAccountIds()));
        group.setStartDate(request.getStartDate());
        group.setEndDate(request.getEndDate());
        group.setDateRange(request.getDateRange());
        group.setDateRangeAmount(request.getDateRangeAmount());
        group.setBudgeted(request.getBudgeted());

        if (request.getQualifiers() != null) {
            request.getQualifiers()
                    .stream()
                    .map(qualifier -> new MoneyTransactionGroupQualifier(
                            existingQualifiersMap.containsKey(qualifier.getId()) ? qualifier.getId() : null,
                            group,
                            qualifier.getQualifierType(),
                            qualifier.getQualifier(),
                            qualifier.getTransactionField()))
                    .forEach(qualifier -> group.getQualifiers().add(qualifier));
        }
        final MoneyTransactionGroupDto dto = moneyTransactionGroupRepository.saveAndFlush(group).toDto();
        if(hasBudget(dto)) {
            budgetValueService.generateValues(userId, dto);
        }
        return dto;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteGroup(final long userId, final long groupId) {
        moneyTransactionGroupRepository.deleteByUserIdAndId(userId, groupId);
    }

    public void groupTransactions(final long userId) {
        accountRepository
                .findByUserId(userId)
                .forEach(account -> groupTransactions(userId, account.getId()));
    }

    public void groupTransactions(final long userId, final long accountId) {
        log.info("Grouping money transactions for {}", accountId);
        final List<MoneyTransactionGroup> groups = moneyTransactionGroupRepository.findByUserId(userId);
        final Map<String, AccountDto> accountsByAccountNumber = accountRepository
                .findByUserId(userId)
                .stream()
                .map(Account::toDto)
                .collect(Collectors.toMap(AccountDto::getAccountNumber, Function.identity()));
        final List<MoneyTransaction> transactions = moneyTransactionRepository.findByAccountId(accountId);
        // todo check
        transactions.forEach(transaction -> groupTransaction(transaction, groups, accountsByAccountNumber));
        moneyTransactionRepository.saveAllAndFlush(transactions);
        log.info("Done grouping {} transactions", transactions.size());
    }

    public List<HistoricalBudgetValueDto> getHistoricalBudgets(final long userId, final LocalDate startDate, final LocalDate endDate) {
        return historicalBudgetValueRepository
                .findByUserIdDateBetween(userId, startDate, endDate)
                .stream()
                .map(HistoricalBudgetValue::toDto)
                .toList();
    }

    public void recalculateHistoricalBudget(final long userId) {
        moneyTransactionGroupRepository
                .findByUserId(userId)
                .stream()
                .filter(this::hasBudget)
                .map(MoneyTransactionGroup::toDto)
                .forEach(group -> budgetValueService.generateValues(userId, group));
    }

    private boolean hasBudget(final MoneyTransactionGroup group) {
        return group.getBudgeted() != null &&
                group.getDateRange() != null &&
                group.getDateRangeAmount() != null &&
                group.getStartDate() != null;
    }

    private void groupTransaction(final MoneyTransaction transaction, final List<MoneyTransactionGroup> groups, final Map<String, AccountDto> accountsByAccountNumber) {
        transaction.setGroupId(null);
        transaction.setLoanId(null);

        for (final MoneyTransactionGroup group : groups) {
            if (appliesToTransaction(transaction, group)) {
                group.getGroupTypes().forEach(groupType -> {
                    switch (groupType) {
                        case MONEY -> transaction.setGroupId(group.getId());
                        case LOAN -> transaction.setLoanId(group.getId()); // todo: why not the loan id itself?
                    }
                });
                break;
            }
        }
        if (accountsByAccountNumber.containsKey(transaction.getCounterpartyAccountNumber())) {
            transaction.setCounterpartyAccountId(accountsByAccountNumber.get(transaction.getCounterpartyAccountNumber()).getId());
        }
    }

    private boolean appliesToTransaction(final MoneyTransaction transaction, final MoneyTransactionGroup group) {
        for (final MoneyTransactionGroupQualifier qualifier : group.getQualifiers()) {
            if (appliesToTransaction(transaction, qualifier)) {
                return true;
            }
        }
        return false;
    }

    private boolean appliesToTransaction(final MoneyTransaction transaction, final MoneyTransactionGroupQualifier qualifier) {
        final String value = getValue(transaction, qualifier);

        if (!StringUtils.hasText(value)) {
            return false;
        }

        try {
            return switch (qualifier.getQualifierType()) {
                case REGEX -> regexAppliesToTransactionValue(value, qualifier.getQualifier());
                case SIMILAR -> clusterAppliesToTransaction(value, qualifier.getQualifier());
                case INCLUDES -> includesAppliesToTransaction(value, qualifier.getQualifier());
            };
        } catch (final Exception e) {
            return false;
        }
    }

    private static String getValue(final MoneyTransaction transaction, final MoneyTransactionGroupQualifier qualifier) {
        final String value = switch (qualifier.getTransactionField()) {
            case DESCRIPTION -> transaction.getDescription();
            case COUNTERPARTY_NAME -> transaction.getCounterpartyAccountName();
            case COUNTERPARTY_NUMBER -> transaction.getCounterpartyAccountNumber();
        };

        return normalizeValue(value);
    }

    private static String normalizeValue(final String value) {
        if (value == null) {
            return null;
        }
        return value
                .replace("\t", "")
                .replace("\r", "")
                .replace("\n", "");
    }

    private boolean includesAppliesToTransaction(final String value, final String qualifier) {
        for (final String search : qualifier.split(",")) {
            if (value.toLowerCase().contains(search.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean regexAppliesToTransactionValue(final String value, final String qualifier) {
        return Pattern.compile(qualifier, Pattern.CASE_INSENSITIVE).matcher(value).matches();
    }

    private boolean clusterAppliesToTransaction(final String value, final String qualifier) {
        final double score = new JaccardSimilarity().apply(value, qualifier);
        return score > 0.6;
    }

    private boolean hasBudget(final MoneyTransactionGroupDto dto) {
        return dto.getBudgeted() != null &&
                dto.getDateRange() != null &&
                dto.getDateRangeAmount() != null &&
                dto.getStartDate() != null;
    }

    @Override
    public boolean canProcess(final Job job) {
        return job.getJobType() == JobType.GROUP_MONEY_TRANSACTIONS;
    }

    @Override
    public void process(final Job job) {
        switch (job.getJobType()) {
            case GROUP_MONEY_TRANSACTIONS -> groupTransactions(job.getUserId());
            default -> throw new IllegalStateException();
        }
    }
}
