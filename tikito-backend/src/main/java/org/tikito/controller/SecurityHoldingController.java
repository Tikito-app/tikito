package org.tikito.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.tikito.auth.AuthUser;
import org.tikito.dto.security.*;
import org.tikito.service.security.SecurityHoldingService;
import org.tikito.service.security.SecurityTransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/security/holding")
@Transactional
public class SecurityHoldingController {

    private final SecurityHoldingService securityHoldingService;
    private final SecurityTransactionService securityTransactionService;

    public SecurityHoldingController(final SecurityHoldingService securityHoldingService,
                                     final SecurityTransactionService securityTransactionService) {
        this.securityHoldingService = securityHoldingService;
        this.securityTransactionService = securityTransactionService;
    }

    @GetMapping
    public ResponseEntity<List<SecurityHoldingDto>> getSecurityHoldings(final AuthUser authUser) {
        return ResponseEntity.ok(securityHoldingService.getSecurityHoldings(authUser.getId()));
    }

    @DeleteMapping("/{securityHoldingId}")
    public ResponseEntity<Void> deleteSecurityHolding(final AuthUser authUser, @PathVariable("securityHoldingId") final long securityHoldingId) {
        securityHoldingService.deleteSecurityHolding(authUser.getId(), securityHoldingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{securityHoldingId}/details")
    public ResponseEntity<SecurityHoldingDto> getSecurityHolding(final AuthUser authUser, @PathVariable("securityHoldingId") final long securityHoldingId) {
        return ResponseEntity.ok(securityHoldingService.getSecurityHolding(authUser.getId(), securityHoldingId));
    }

    @PostMapping("/transactions")
    public ResponseEntity<List<SecurityTransactionDto>> getSecurityTransactions(final AuthUser authUser, @Valid @RequestBody final SecurityHoldingFilter filter) {
        return ResponseEntity.ok(securityTransactionService.getSecurityTransactions(authUser.getId(), filter));
    }

    @PostMapping("/historical-values")
    public ResponseEntity<List<HistoricalSecurityHoldingValueDto>> getHistoricalHoldingValues(final AuthUser authUser, @Valid @RequestBody final SecurityHoldingFilter filter) {
        return ResponseEntity.ok(securityHoldingService.getHistoricalHoldingValues(authUser.getId(), filter));
    }

    @GetMapping("/aggregated-historical-values")
    public ResponseEntity<List<AggregatedHistoricalSecurityHoldingValueDto>> getAggregatedHistoricalHoldingValues(final AuthUser authUser) {
        return ResponseEntity.ok(securityHoldingService.getAggregatedHistoricalHoldingValues(authUser.getId()));
    }
}