package org.tikito.controller;

import org.tikito.auth.AuthUser;
import org.tikito.dto.security.*;
import org.tikito.service.security.SecurityHoldingService;
import org.tikito.service.security.SecurityTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/security/holding")
@Transactional
public class SecurityHoldingController {

    private final SecurityTransactionService securityTransactionService;
    private final SecurityHoldingService securityHoldingService;

    public SecurityHoldingController(final SecurityTransactionService securityTransactionService,
                                     final SecurityHoldingService securityHoldingService) {
        this.securityTransactionService = securityTransactionService;
        this.securityHoldingService = securityHoldingService;
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
    public ResponseEntity<List<SecurityTransactionDto>> getSecurityTransactions(final AuthUser authUser, @RequestBody final SecurityHoldingFilter filter) {
        return ResponseEntity.ok(securityTransactionService.getSecurityTransactions(authUser.getId(), filter));
    }

    @PostMapping("/historical-values")
    public ResponseEntity<List<HistoricalSecurityHoldingValueDto>> getHistoricalHoldingValues(final AuthUser authUser, @RequestBody final SecurityHoldingFilter filter) {
        return ResponseEntity.ok(securityHoldingService.getHistoricalHoldingValues(authUser.getId(), filter));
    }

    @GetMapping("/aggregated-historical-values")
    public ResponseEntity<List<AggregatedHistoricalSecurityHoldingValueDto>> getAggregatedHistoricalHoldingValues(final AuthUser authUser) {
        return ResponseEntity.ok(securityHoldingService.getAggregatedHistoricalHoldingValues(authUser.getId()));
    }

    @GetMapping("/{ids}/multiple-historical-values")
    public ResponseEntity<List<HistoricalSecurityHoldingValueDto>> getHistoricalHoldingValues(final AuthUser authUser, @PathVariable("ids") final String ids) {
        return ResponseEntity.ok(securityHoldingService.getHistoricalHoldingValues(
                authUser.getId(),
                Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toSet())));
    }
}