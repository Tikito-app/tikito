package org.tikito.controller;

import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tikito.auth.AuthUser;
import org.tikito.controller.request.CreateOrUpdateLoanPartRequest;
import org.tikito.controller.request.CreateOrUpdateLoanRequest;
import org.tikito.dto.loan.LoanDto;
import org.tikito.dto.loan.LoanPartDto;
import org.tikito.dto.loan.LoanValueDto;
import org.tikito.service.LoanService;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
@Transactional
public class LoanController {
    private final LoanService loanService;

    public LoanController(final LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public ResponseEntity<List<LoanDto>> getLoans(final AuthUser authUser) {
        return ResponseEntity.ok(loanService.getLoans(authUser.getId()));
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDto> getLoan(final AuthUser authUser, @PathVariable("loanId") final long loanId) {
        return ResponseEntity.ok(loanService.getLoan(authUser.getId(), loanId));
    }

    @PostMapping
    public ResponseEntity<LoanDto> createOrUpdateLoan(final AuthUser authUser, @Validated @RequestBody final CreateOrUpdateLoanRequest request) {
        return ResponseEntity.ok(loanService.createOrUpdateLoan(authUser.getId(), request));
    }

    @PostMapping("/part")
    public ResponseEntity<LoanPartDto> createOrUpdateLoanPart(final AuthUser authUser, @Validated @RequestBody final CreateOrUpdateLoanPartRequest request) {
        final LoanPartDto loanPart = loanService.createOrUpdateLoanPart(authUser.getId(), request);
        return ResponseEntity.ok(loanPart);
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<Void> deleteLoan(final AuthUser authUser, @PathVariable("loanId") final long loanId) {
        loanService.deleteLoan(authUser.getId(), loanId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/values")
    public ResponseEntity<List<LoanValueDto>> getLoanValues(final AuthUser authUser) {
        return ResponseEntity.ok(loanService.getLoanValues(authUser.getId()));
    }

    @GetMapping("/values/current")
    public ResponseEntity<List<LoanValueDto>> getCurrentLoanValues(final AuthUser authUser) {
        return ResponseEntity.ok(loanService.getCurrentLoanValues(authUser.getId()));
    }

    @DeleteMapping("/{loanId}/part/{loanPartId}")
    public ResponseEntity<Void> deleteLoanPart(final AuthUser authUser, @PathVariable("loanId") final long loanId, @PathVariable("loanPartId") final long loanPartId) {
        loanService.deleteLoanPart(authUser.getId(), loanId, loanPartId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{loanId}/part/{loanPartId}/interest/{interestId}")
    public ResponseEntity<Void> deleteLoanInterest(final AuthUser authUser, @PathVariable("loanId") final long loanId, @PathVariable("loanPartId") final long loanPartId, @PathVariable("interestId") final long loanInterestId) {
        loanService.deleteLoanInterest(authUser.getId(), loanId, loanPartId, loanInterestId);
        return ResponseEntity.ok().build();
    }
}
