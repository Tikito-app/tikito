package org.tikito.service;

import org.tikito.controller.request.CreateOrUpdateBudgetRequest;
import org.tikito.dto.budget.BudgetDto;
import org.tikito.dto.budget.HistoricalBudgetValueDto;
import org.tikito.dto.money.MoneyTransactionFilter;
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

import java.time.LocalDate;
import java.util.*;

@Service
public class BudgetService {
    private final HistoricalBudgetValueRepository historicalBudgetValueRepository;
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;
    private final AccountRepository accountRepository;
    private final BudgetValueService budgetValueService;

    public BudgetService(final HistoricalBudgetValueRepository historicalBudgetValueRepository,
                         final MoneyTransactionGroupRepository moneyTransactionGroupRepository,
                         final AccountRepository accountRepository,
                         final BudgetValueService budgetValueService) {
        this.historicalBudgetValueRepository = historicalBudgetValueRepository;
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
        this.accountRepository = accountRepository;
        this.budgetValueService = budgetValueService;
    }


    private void assertAccountIdsExists(final Set<Long> accountIds) {
        if(accountIds != null) {
            if(accountRepository.findAllById(accountIds).size() != accountIds.size()) {
                throw new NoSuchElementException();
            }
        }
    }
}
