package org.tikito.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tikito.auth.AuthUser;
import org.tikito.dto.money.AggregatedHistoricalMoneyHoldingValueDto;
import org.tikito.dto.money.HistoricalMoneyHoldingValueDto;
import org.tikito.dto.money.MoneyHoldingDto;
import org.tikito.dto.money.MoneyTransactionFilter;
import org.tikito.service.money.MoneyHoldingService;

import java.util.List;

@RestController
@RequestMapping("/api/money")
public class MoneyController {
    private final MoneyHoldingService moneyHoldingService;

    public MoneyController(final MoneyHoldingService moneyHoldingService) {
        this.moneyHoldingService = moneyHoldingService;
    }

    @GetMapping
    public ResponseEntity<List<MoneyHoldingDto>> getHoldings(final AuthUser user) {
        return ResponseEntity.ok(moneyHoldingService.getMoneyHoldings(user.getId()));
    }

    @PostMapping("/historical-values")
    public ResponseEntity<List<HistoricalMoneyHoldingValueDto>> getHistoricalMoneyHoldingValue(final AuthUser user, @Valid @RequestBody MoneyTransactionFilter filter) {
        return ResponseEntity.ok(moneyHoldingService.getHistoricalMoneyHoldingValue(user.getId(), filter));
    }
}
