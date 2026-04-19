package org.tikito.controller;

import org.springframework.validation.annotation.Validated;
import org.tikito.auth.AuthUser;
import org.tikito.controller.request.CreateOrUpdateMoneyTransactionGroupRequest;
import org.tikito.dto.money.HistoricalBudgetValueDto;
import org.tikito.dto.money.MoneyTransactionGroupDto;
import org.tikito.service.money.MoneyTransactionGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/money/transactions-group")
@Transactional
public class MoneyTransactionGroupController {
    private final MoneyTransactionGroupService groupService;

    public MoneyTransactionGroupController(final MoneyTransactionGroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<List<MoneyTransactionGroupDto>> getGroups(final AuthUser authUser) {
        return ResponseEntity.ok(groupService.getGroups(authUser.getId()));
    }

    @GetMapping("/{groupId}/details")
    public ResponseEntity<MoneyTransactionGroupDto> getGroup(final AuthUser authUser, @PathVariable("groupId") final long groupId) {
        return ResponseEntity.ok(groupService.getGroup(authUser.getId(), groupId));
    }

    @PostMapping
    public ResponseEntity<MoneyTransactionGroupDto> createOrUpdate(final AuthUser authUser, @Validated @RequestBody final CreateOrUpdateMoneyTransactionGroupRequest request) {
        return ResponseEntity.ok(groupService.createOrUpdateGroup(authUser.getId(), request));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(final AuthUser authUser, @PathVariable("groupId") final long groupId) {
        groupService.deleteGroup(authUser.getId(), groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/historical-budget-values/{startDate}/{endDate}")
    public ResponseEntity<List<HistoricalBudgetValueDto>> getHistoricalBudgets(final AuthUser authUser, @PathVariable("startDate") final LocalDate startDate, @PathVariable("endDate") final LocalDate endDate) {
        return ResponseEntity.ok(groupService.getHistoricalBudgets(authUser.getId(), startDate, endDate));
    }

    @GetMapping("/recalculate-historical-budget")
    public ResponseEntity<Void> recalculateHistoricalBudget(final AuthUser authUser) {
        groupService.recalculateHistoricalBudget(authUser.getId());
        return ResponseEntity.ok().build();
    }
}
