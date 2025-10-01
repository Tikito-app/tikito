package org.tikito.service;

import org.tikito.controller.request.CreateOrUpdateBudgetRequest;
import org.tikito.dto.money.MoneyTransactionGroupDto;
import org.tikito.dto.budget.BudgetDto;
import org.tikito.dto.budget.HistoricalBudgetDto;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.budget.Budget;
import org.tikito.entity.budget.HistoricalBudget;
import org.tikito.repository.AccountRepository;
import org.tikito.repository.MoneyTransactionGroupRepository;
import org.tikito.repository.BudgetRepository;
import org.tikito.repository.HistoricalBudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final HistoricalBudgetRepository historicalBudgetRepository;
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;
    private final AccountRepository accountRepository;

    public BudgetService(final BudgetRepository budgetRepository,
                         final HistoricalBudgetRepository historicalBudgetRepository,
                         final MoneyTransactionGroupRepository moneyTransactionGroupRepository,
                         final AccountRepository accountRepository) {
        this.budgetRepository = budgetRepository;
        this.historicalBudgetRepository = historicalBudgetRepository;
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
        this.accountRepository = accountRepository;
    }

    public List<BudgetDto> getBudgets(final long userId) {
        return budgetRepository
                .findByUserId(userId)
                .stream()
                .map(Budget::toDto)
                .toList();
    }

    public BudgetDto getBudget(final long userId, final long budgetId) {
        return budgetRepository
                .findByUserIdAndId(userId, budgetId)
                .orElseThrow()
                .toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public BudgetDto createOrUpdateBudget(final long userId, final CreateOrUpdateBudgetRequest request) {
        final Budget budget = request.isNew() ? new Budget(userId) : budgetRepository.findByUserIdAndId(userId, request.getId()).orElseThrow();
        assertAccountIdsExists(request.getAccountIds());
        final List<MoneyTransactionGroup> groups = moneyTransactionGroupRepository.findAllById(request.getGroupIds());
        if(groups.size() != request.getGroupIds().size()) {
            throw new NoSuchElementException();
        }
        budget.setName(request.getName());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setDateRange(request.getDateRange());
        budget.setAmount(request.getAmount());
        budget.setAccountIds(request.getAccountIds());
        budget.setGroups(groups);

        // todo: what about the unselected groups. We need to set the budget_id to null
        final Budget entity = budgetRepository.saveAndFlush(budget);
        groups.forEach(group -> group.setBudget(entity));
        moneyTransactionGroupRepository.saveAllAndFlush(groups);
        return entity.toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteBudget(final long userId, final long budgetId) {
        historicalBudgetRepository.deleteByUserIdAndBudgetId(userId, budgetId);
    }

    public List<HistoricalBudgetDto> getHistoricalBudgets(final long userId) {
        return historicalBudgetRepository
                .findByUserId(userId)
                .stream()
                .map(HistoricalBudget::toDto)
                .toList();
    }

    public void recalculateHistoricalBudget(final long userId) {

    }

    public List<MoneyTransactionGroupDto> getAvailableMoneyTransactionGroups(final long userId) {
        return getAvailableMoneyTransactionGroups(userId, Optional.empty());
    }

    public List<MoneyTransactionGroupDto> getAvailableMoneyTransactionGroups(final long userId, final Optional<Long> budgetId) {
        final Set<Long> existingGroupIdsUsed = budgetRepository
                .findByUserId(userId)
                .stream()
                .filter(budget -> budgetId.isEmpty() || !Objects.equals(budget.getId(), budgetId.get()))
                .map(Budget::getGroups)
                .flatMap(Collection::stream)
                .map(MoneyTransactionGroup::getId)
                .collect(Collectors.toSet());

        return moneyTransactionGroupRepository
                .findByUserId(userId)
                .stream()
                .filter(group -> !existingGroupIdsUsed.contains(group.getId()))
                .map(MoneyTransactionGroup::toDto)
                .toList();
    }

    private void assertAccountIdsExists(final Set<Long> accountIds) {
        if(accountIds != null) {
            if(accountRepository.findAllById(accountIds).size() != accountIds.size()) {
                throw new NoSuchElementException();
            }
        }
    }
}
