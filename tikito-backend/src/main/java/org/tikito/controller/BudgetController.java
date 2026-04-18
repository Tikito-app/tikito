//package org.tikito.controller;
//
//import jakarta.transaction.Transactional;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.tikito.auth.AuthUser;
//import org.tikito.controller.request.CreateOrUpdateBudgetRequest;
//import org.tikito.dto.budget.BudgetDto;
//import org.tikito.dto.budget.HistoricalBudgetValueDto;
//import org.tikito.dto.money.MoneyTransactionFilter;
//import org.tikito.service.BudgetService;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/budget")
//@Transactional
//public class BudgetController {
//    private final BudgetService budgetService;
//
//    public BudgetController(final BudgetService budgetService) {
//        this.budgetService = budgetService;
//    }
//
//    @GetMapping("/historical-values/{startDate}/{endDate}")
//    public ResponseEntity<List<HistoricalBudgetValueDto>> getHistoricalBudgets(final AuthUser authUser, @PathVariable("startDate") final LocalDate startDate, @PathVariable("endDate") final LocalDate endDate) {
//        return ResponseEntity.ok(budgetService.getHistoricalBudgets(authUser.getId(), startDate, endDate));
//    }
//
//    @GetMapping("/recalculate-historical-budget")
//    public ResponseEntity<Void> recalculateHistoricalBudget(final AuthUser authUser) {
//        budgetService.recalculateHistoricalBudget(authUser.getId());
//        return ResponseEntity.ok().build();
//    }
//
//}
