package org.tikito.service.money;

import org.tikito.controller.request.CreateOrUpdateMoneyTransactionGroupRequest;
import org.tikito.dto.AccountDto;
import org.tikito.dto.AccountType;
import org.tikito.dto.money.MoneyTransactionGroupDto;
import org.tikito.entity.Account;
import org.tikito.entity.Job;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.money.MoneyTransactionGroupQualifier;
import org.tikito.repository.AccountRepository;
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
import org.tikito.service.job.JobProcessor;
import org.tikito.service.job.JobType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MoneyTransactionGroupService implements JobProcessor {
    private final MoneyTransactionGroupRepository groupRepository;
    private final AccountRepository accountRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;

    public MoneyTransactionGroupService(final MoneyTransactionGroupRepository groupRepository,
                                        final AccountRepository accountRepository,
                                        final MoneyTransactionRepository moneyTransactionRepository) {
        this.groupRepository = groupRepository;
        this.accountRepository = accountRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
    }

    public List<MoneyTransactionGroupDto> getGroups(final long userId) {
        return groupRepository
                .findByUserId(userId)
                .stream()
                .map(MoneyTransactionGroup::toDto)
                .sorted(Comparator.comparing(MoneyTransactionGroupDto::getName))
                .toList();
    }

    public MoneyTransactionGroupDto getGroup(final long userId, final long groupId) {
        return groupRepository
                .findByUserIdAndId(userId, groupId)
                .orElseThrow()
                .toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MoneyTransactionGroupDto createOrUpdateGroup(final long userId, final @Valid @NotNull CreateOrUpdateMoneyTransactionGroupRequest request) {
        final MoneyTransactionGroup group;

        if (request.getId() != 0) {
            group = groupRepository.findByUserIdAndId(userId, request.getId()).orElseThrow();
        } else {
            group = new MoneyTransactionGroup();
            group.setUserId(userId);
            group.setQualifiers(new ArrayList<>());
        }
        final Map<Long, MoneyTransactionGroupQualifier> existingQualifiersMap = group.getQualifiers().stream().collect(Collectors.toMap(MoneyTransactionGroupQualifier::getId, Function.identity()));
        group.setName(request.getName());
        group.getQualifiers().clear();
        if (request.getQualifiers() != null) {
            request
                    .getQualifiers()
                    .stream()
                    .map(qualifier -> new MoneyTransactionGroupQualifier(
                            existingQualifiersMap.containsKey(qualifier.getId()) ? qualifier.getId() : null,
                            group,
                            qualifier.getQualifierType(),
                            qualifier.getQualifier(),
                            qualifier.getTransactionField()))
                    .forEach(qualifier -> group.getQualifiers().add(qualifier));
        }
        return groupRepository.saveAndFlush(group).toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteGroup(final long userId, final long groupId) {
        groupRepository.deleteByUserIdAndId(userId, groupId);
    }

    public void groupTransactions(final long userId) {
        accountRepository
                .findByUserIdAndAccountType(userId, AccountType.DEBIT)
                .forEach(account -> groupTransactions(userId, account.getId()));
    }

    public void groupTransactions(final long userId, final long accountId) {
        log.info("Grouping money transactions for {}", accountId);
        final List<MoneyTransactionGroup> groups = groupRepository.findByUserId(userId);
        final Map<String, AccountDto> accountsByAccountNumber = accountRepository
                .findAll()
                .stream()
                .map(Account::toDto)
                .collect(Collectors.toMap(AccountDto::getAccountNumber, Function.identity()));
        final List<MoneyTransaction> transactions = moneyTransactionRepository.findByAccountId(accountId);
        transactions.forEach(transaction -> groupTransaction(transaction, groups, accountsByAccountNumber));
        moneyTransactionRepository.saveAllAndFlush(transactions);
        log.info("Done grouping");
    }

    private void groupTransaction(final MoneyTransaction transaction, final List<MoneyTransactionGroup> groups, final Map<String, AccountDto> accountsByAccountNumber) {
        for (final MoneyTransactionGroup group : groups) {
            if (appliesToTransaction(transaction, group)) {
                transaction.setGroupId(group.getId());
                return;
            }
        }
        if (accountsByAccountNumber.containsKey(transaction.getCounterpartAccountNumber())) {
            transaction.setCounterpartAccountId(accountsByAccountNumber.get(transaction.getCounterpartAccountNumber()).getId());
        }
        transaction.setGroupId(null);
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
            case COUNTERPARTY_NAME -> transaction.getCounterpartAccountName();
            case COUNTERPARTY_NUMBER -> transaction.getCounterpartAccountNumber();
        };

        return normalizeValue(value);
    }

    private static String normalizeValue(final String value) {
        if (value == null) {
            return null;
        }
        return value
                .replaceAll("\t", "")
                .replaceAll("\r", "")
                .replaceAll("\n", "");
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
