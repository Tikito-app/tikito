package org.tikito.controller;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.tikito.auth.AuthUser;
import org.tikito.controller.request.CreateOrUpdateAccountRequest;
import org.tikito.dto.AccountDto;
import org.tikito.dto.AssetType;
import org.tikito.service.AccountService;
import org.tikito.service.importer.money.ABNFileParser;
import org.tikito.service.importer.money.BitvavoFileParser;
import org.tikito.service.importer.money.INGFileParser;
import org.tikito.service.importer.security.DeGiroAccountImporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@Transactional
public class AccountController {
    private final AccountService accountService;

    public AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAccounts(final AuthUser authUser) {
        return ResponseEntity.ok(accountService.getAccounts(authUser.getId()));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(final AuthUser authUser, @PathVariable("accountId") final Long accountId) {
        return ResponseEntity.ok(accountService.getAccount(authUser.getId(), accountId));
    }

    @PostMapping
    public ResponseEntity<AccountDto> createOrUpdateAccount(final AuthUser authUser, @Valid @RequestBody final CreateOrUpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.createOrUpdate(authUser.getId(), request));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<AccountDto> deleteAccount(final AuthUser authUser, @PathVariable("accountId") final Long accountId) {
        accountService.deleteAccount(authUser.getId(), accountId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/importer-types-headers")
    public ResponseEntity<Map<String, ImportTypeData>> getImporterTypesHeaders() {
        final Map<String, ImportTypeData> map = new HashMap<>();
        map.put("ABN", ImportTypeData.builder().assetType(AssetType.FIAT).headers(new ABNFileParser().getHeaders()).build());
        map.put("ING", ImportTypeData.builder().assetType(AssetType.FIAT).headers(new INGFileParser().getHeaders()).build());
        map.put("DE_GIRO", ImportTypeData.builder().assetType(AssetType.SECURITY).headers(new DeGiroAccountImporter().getHeaders()).build());
        map.put("BITVAVO", ImportTypeData.builder().assetType(AssetType.CRYPTO).headers(new BitvavoFileParser().getHeaders()).build());
        return ResponseEntity.ok(map);
    }

    @Builder
    @Getter
    public static class ImportTypeData {
        private AssetType assetType;
        private List<String> headers;
    }
}