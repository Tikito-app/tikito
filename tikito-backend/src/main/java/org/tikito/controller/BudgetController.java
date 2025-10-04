package org.tikito.controller;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tikito.auth.AuthUser;
import org.tikito.controller.request.CreateOrUpdateBudgetRequest;
import org.tikito.dto.budget.BudgetDto;
import org.tikito.dto.budget.HistoricalBudgetValueDto;
import org.tikito.service.BudgetService;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
@Transactional
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(final BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<List<BudgetDto>> getBudgets(final AuthUser authUser) {
        return ResponseEntity.ok(budgetService.getBudgets(authUser.getId()));
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetDto> getBudget(final AuthUser authUser, @PathVariable("budgetId") final long budgetId) {
        return ResponseEntity.ok(budgetService.getBudget(authUser.getId(), budgetId));
    }

    @PostMapping
    public ResponseEntity<BudgetDto> createOrUpdateBudget(final AuthUser authUser, @RequestBody final CreateOrUpdateBudgetRequest request) {
        return ResponseEntity.ok(budgetService.createOrUpdateBudget(authUser.getId(), request));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(final AuthUser authUser, @PathVariable("budgetId") final long budgetId) {
        budgetService.deleteBudget(authUser.getId(), budgetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/historical-values")
    public ResponseEntity<List<HistoricalBudgetValueDto>> getHistoricalBudgets(final AuthUser authUser) {
        return ResponseEntity.ok(budgetService.getHistoricalBudgets(authUser.getId()));
    }

    @GetMapping("/recalculate-historical-budget")
    public ResponseEntity<Void> recalculateHistoricalBudget(final AuthUser authUser) {
        budgetService.recalculateHistoricalBudget(authUser.getId());
        return ResponseEntity.ok().build();
    }

}
