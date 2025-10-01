package org.tikito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tikito.auth.AuthUser;
import org.tikito.controller.request.AdminEditSecurityRequest;
import org.tikito.controller.request.AdminEditUserRequest;
import org.tikito.controller.request.UpdateIsinRequest;
import org.tikito.dto.UserAccountDto;
import org.tikito.dto.export.TikitoExportDto;
import org.tikito.dto.security.IsinDto;
import org.tikito.dto.security.SecurityDto;
import org.tikito.entity.Job;
import org.tikito.exception.PasswordNotLongEnoughException;
import org.tikito.service.export.ImportExportService;
import org.tikito.service.JobService;
import org.tikito.service.UserAccountService;
import org.tikito.service.job.JobType;
import org.tikito.service.security.SecurityService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Transactional
public class AdminController {
    private final UserAccountService userAccountService;
    private final SecurityService securityService;
    private final JobService jobService;
    private final ImportExportService importExportService;

    public AdminController(final UserAccountService userAccountService,
                           final SecurityService securityService,
                           final JobService jobService,
                           final ImportExportService importExportService) {
        this.userAccountService = userAccountService;
        this.securityService = securityService;
        this.jobService = jobService;
        this.importExportService = importExportService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserAccountDto>> getUsers(final AuthUser authUser) {
        return ResponseEntity.ok(userAccountService.getUsers());
    }

    @PostMapping("/users/{userAccountId}")
    public ResponseEntity<UserAccountDto> editUser(final AuthUser authUser, @PathVariable("userAccountId") final long userAccountId, @Validated @RequestBody final AdminEditUserRequest request) throws PasswordNotLongEnoughException {
        return ResponseEntity.ok(userAccountService.editUser(userAccountId, request.getEmail(), request.getPassword()));
    }

    @GetMapping("/securities")
    public ResponseEntity<List<SecurityDto>> getSecurities(final AuthUser authUser) {
        return ResponseEntity.ok(securityService.getSecurities());
    }

    @GetMapping("/securities/{securityId}")
    public ResponseEntity<SecurityDto> getSecurities(final AuthUser authUser, @PathVariable("securityId") final long securityId) {
        return ResponseEntity.ok(securityService.getSecurity(securityId));
    }

    @PostMapping("/securities/{securityId}")
    public ResponseEntity<SecurityDto> editSecurity(final AuthUser authUser, @PathVariable("securityId") final long securityId, @Validated @RequestBody final AdminEditSecurityRequest request) {
        return ResponseEntity.ok(securityService.editSecurity(securityId, request.getName()));
    }

    @DeleteMapping("/securities/{securityId}")
    public ResponseEntity<Void> deleteSecurity(final AuthUser authUser, @PathVariable("securityId") final long securityId) {
//        securityService.deleteSecurity(securityId);
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/securities/update-all")
    public ResponseEntity<Void> updateAllSecurities(final AuthUser authUser) {
        jobService.updateAllSecurities(authUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/securities/update-all-values")
    public ResponseEntity<Void> updateAllSecurityValues(final AuthUser authUser) {
        jobService.updateAllSecurityValues(authUser.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/securities/{securityId}/recalculate-historical-value")
    public ResponseEntity<Void> recalculateHistoricalSecurityValue(final AuthUser authUser, @PathVariable("securityId") final long securityId) {
        jobService.addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, securityId, authUser.getId()).build());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/securities/{securityId}/update-prices")
    public ResponseEntity<Void> updateSecurityPrices(final AuthUser authUser, @PathVariable("securityId") final long securityId) {
        jobService.addJob(Job.security(JobType.UPDATE_SECURITY_PRICES, securityId, authUser.getId()).build());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/securities/{securityId}/enrich")
    public ResponseEntity<Void> enrichSecurity(final AuthUser authUser, @PathVariable("securityId") final long securityId) {
        jobService.addJob(Job.security(JobType.ENRICH_SECURITY, securityId, authUser.getId()).build());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/securities/{securityId}/delete-prices")
    public ResponseEntity<Void> deleteSecurityPrices(final AuthUser authUser, @PathVariable("securityId") final long securityId) {
        jobService.addJob(Job.security(JobType.DELETE_SECURITY_PRICES, securityId, authUser.getId()).build());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/securities/isin/{isin}")
    public ResponseEntity<IsinDto> getIsin(final AuthUser authUser, @PathVariable("isin") final String isin) {
        return ResponseEntity.ok(securityService.getIsin(isin));
    }

    @PostMapping("/securities/isin/{isin}")
    public ResponseEntity<IsinDto> updateIsin(final AuthUser authUser, @PathVariable("isin") final String isin, @Validated @RequestBody final UpdateIsinRequest request) {
        return ResponseEntity.ok(securityService.updateIsin(isin, request.getSymbol(), request.getValidFrom(), request.getValidTo()));
    }

    @DeleteMapping("/securities/isin{isin}")
    public ResponseEntity<Void> deleteIsin(final AuthUser authUser, @PathVariable("isin") final String isin) {
        securityService.deleteIsin(isin);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/money/{accountId}/recalculate-historical-value")
    public ResponseEntity<Void> recalculateHistoricalMoneyValue(final AuthUser authUser, @PathVariable("accountId") final long accountId) {
        jobService.addJob(Job.account(JobType.RECALCULATE_HISTORICAL_MONEY_VALUES, accountId, authUser.getId()).build());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/money/group-transactions")
    public ResponseEntity<Void> groupMoneyTransaction(final AuthUser authUser) {
        jobService.addJob(Job.account(JobType.GROUP_MONEY_TRANSACTIONS, authUser.getId()).build());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<TikitoExportDto> export(final AuthUser authUser) {
        return ResponseEntity.ok(importExportService.export(authUser.getId()));
    }

    @PostMapping("/import")
    public ResponseEntity<Void> doImport(final AuthUser authUser, @Validated @RequestBody final TikitoExportDto dto) {
        importExportService.importFrom(authUser.getId(), dto);
        return ResponseEntity.ok().build();
    }
}
