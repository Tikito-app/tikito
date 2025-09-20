package org.tikito.controller;

import org.tikito.auth.AuthUser;
import org.tikito.dto.money.AggregatedHistoricalMoneyHoldingValueDto;
import org.tikito.service.money.MoneyHoldingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/money/holding")
public class MoneyHoldingController {
    private final MoneyHoldingService moneyHoldingService;

    public MoneyHoldingController(final MoneyHoldingService moneyHoldingService) {
        this.moneyHoldingService = moneyHoldingService;
    }

    @GetMapping("/aggregated-historical-values")
    public ResponseEntity<List<AggregatedHistoricalMoneyHoldingValueDto>> getAggregatedValues(final AuthUser user) {
        return ResponseEntity.ok(moneyHoldingService.getAggregatedHoldingValues(user.getId()));
    }
}
