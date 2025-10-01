package org.tikito.controller;

import org.springframework.validation.annotation.Validated;
import org.tikito.auth.AuthUser;
import org.tikito.controller.request.SetMoneyTransactionGroupIdRequest;
import org.tikito.dto.money.MoneyTransactionDto;
import org.tikito.dto.money.MoneyTransactionFilter;
import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.exception.CannotReadFileException;
import org.tikito.service.money.MoneyImportService;
import org.tikito.service.money.MoneyTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/money/transaction")
@Transactional
public class MoneyTransactionController {
    private final MoneyTransactionService moneyTransactionService;
    private final MoneyImportService moneyImportService;

    public MoneyTransactionController(final MoneyTransactionService moneyTransactionService,
                                      final MoneyImportService moneyImportService) {
        this.moneyTransactionService = moneyTransactionService;
        this.moneyImportService = moneyImportService;
    }

    @PostMapping
    public ResponseEntity<List<MoneyTransactionDto>> getTransactions(final AuthUser authUser, @Validated @RequestBody final MoneyTransactionFilter filter) {
        return ResponseEntity.ok(moneyTransactionService.getTransactions(authUser.getId(), filter));
    }

    @GetMapping("/loan")
    public ResponseEntity<List<MoneyTransactionDto>> getTransactionsForLoans(final AuthUser authUser) {
        return ResponseEntity.ok(moneyTransactionService.getTransactionsForLoans(authUser.getId()));
    }

    @PostMapping
    @RequestMapping("/set-group-id")
    public ResponseEntity<MoneyTransaction> setTransactionGroupId(final AuthUser authUser, @Validated @RequestBody final SetMoneyTransactionGroupIdRequest request) {
        return ResponseEntity.ok(moneyTransactionService.setTransactionGroupId(authUser.getId(), request.getTransactionId(), request.getGroupId()));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(final AuthUser authUser, @PathVariable("transactionId") final long transactionId) {
        moneyTransactionService.deleteTransaction(authUser.getId(), transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountId}/import")
    public ResponseEntity<List<MoneyTransactionImportLine>> importTransactions(final AuthUser authUser,
                                                                               @PathVariable("accountId") final long accountId,
                                                                               @RequestParam("file") final MultipartFile file,
                                                                               @RequestParam(name = "csv-separator", required = false) final String csvSeparator,
                                                                               @RequestParam(name = "header-config", required = false) final String customHeaderConfigString,
                                                                               @RequestParam(name = "debit-indication", required = false) final String debitIndication,
                                                                               @RequestParam(name = "timestamp-format", required = false) final String timestampFormat,
                                                                               @RequestParam(name = "date-format", required = false) final String dateFormat,
                                                                               @RequestParam(name = "time-format", required = false) final String timeFormat,
                                                                               @RequestParam("dryRun") final boolean dryRun) throws CannotReadFileException {
        try {
            return ResponseEntity.ok(moneyImportService
                    .importTransactions(authUser.getId(), accountId, file, dryRun, customHeaderConfigString, debitIndication, timestampFormat, dateFormat, timeFormat, csvSeparator)
                    .getLines());
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
