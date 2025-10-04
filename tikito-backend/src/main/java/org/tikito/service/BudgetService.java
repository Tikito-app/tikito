package org.tikito.service;

import org.tikito.controller.request.CreateOrUpdateBudgetRequest;
import org.tikito.dto.budget.BudgetDto;
import org.tikito.dto.budget.HistoricalBudgetValueDto;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.budget.Budget;
import org.tikito.entity.budget.HistoricalBudgetValue;
import org.tikito.repository.AccountRepository;
import org.tikito.repository.MoneyTransactionGroupRepository;
import org.tikito.repository.BudgetRepository;
import org.tikito.repository.HistoricalBudgetValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final HistoricalBudgetValueRepository historicalBudgetValueRepository;
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;
    private final AccountRepository accountRepository;
    private final BudgetValueService budgetValueService;

    public BudgetService(final BudgetRepository budgetRepository,
                         final HistoricalBudgetValueRepository historicalBudgetValueRepository,
                         final MoneyTransactionGroupRepository moneyTransactionGroupRepository,
                         final AccountRepository accountRepository,
                         final BudgetValueService budgetValueService) {
        this.budgetRepository = budgetRepository;
        this.historicalBudgetValueRepository = historicalBudgetValueRepository;
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
        this.accountRepository = accountRepository;
        this.budgetValueService = budgetValueService;
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
        final List<MoneyTransactionGroup> groups = moneyTransactionGroupRepository.findAllByIdsOrBudgetId(request.getGroupIds(), request.getId());

        budget.setName(request.getName());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setDateRange(request.getDateRange());
        budget.setDateRangeAmount(request.getDateRangeAmount());
        budget.setAmount(request.getAmount());
        budget.setAccountIds(request.getAccountIds());
        budget.setGroups(groups);

        final Budget entity = budgetRepository.saveAndFlush(budget);
        groups.forEach(group -> {
            group.setBudget(request.getGroupIds().contains(group.getId()) ? entity : null);
        });
        moneyTransactionGroupRepository.saveAllAndFlush(groups);
        budgetValueService.generateValues(userId, budget.getId());
        return entity.toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteBudget(final long userId, final long budgetId) {
        historicalBudgetValueRepository.deleteByUserIdAndBudgetId(userId, budgetId);
        budgetRepository.deleteByUserIdAndId(userId, budgetId);
    }

    public List<HistoricalBudgetValueDto> getHistoricalBudgets(final long userId) {
        return historicalBudgetValueRepository
                .findByUserId(userId)
                .stream()
                .map(HistoricalBudgetValue::toDto)
                .toList();
    }

    public void recalculateHistoricalBudget(final long userId) {
        budgetRepository
                .findByUserId(userId)
                .forEach(budget -> budgetValueService.generateValues(userId, budget.getId()));
    }

    private void assertAccountIdsExists(final Set<Long> accountIds) {
        if(accountIds != null) {
            if(accountRepository.findAllById(accountIds).size() != accountIds.size()) {
                throw new NoSuchElementException();
            }
        }
    }
}
