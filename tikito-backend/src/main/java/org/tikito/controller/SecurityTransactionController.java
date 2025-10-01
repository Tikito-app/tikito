package org.tikito.controller;

import org.tikito.auth.AuthUser;
import org.tikito.dto.security.SecurityTransactionDto;
import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionImportResultDto;
import org.tikito.exception.UnsupportedImportFormatException;
import org.tikito.service.security.SecurityImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tikito.service.security.SecurityTransactionService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/security/transaction")
@Transactional
public class SecurityTransactionController {
    private final SecurityImportService securityImportService;
    private final SecurityTransactionService securityTransactionService;

    public SecurityTransactionController(final SecurityImportService securityImportService,
                                         final SecurityTransactionService securityTransactionService) {
        this.securityImportService = securityImportService;
        this.securityTransactionService = securityTransactionService;
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<SecurityTransactionDto> deleteSecurityTransaction(final AuthUser authUser, @PathVariable("transactionId") final long transactionId) {
        securityTransactionService.deleteTransaction(authUser.getId(), transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{accountId}/import")
    public ResponseEntity<List<SecurityTransactionImportLine>> importTransactions(final AuthUser authUser,
                                                                                  @PathVariable("accountId") final long accountId,
                                                                                  @RequestParam("file") final MultipartFile file,
                                                                                  @RequestParam("timestamp-format") final String timestampFormat,
                                                                                  @RequestParam("date-format") final String dateFormat,
                                                                                  @RequestParam("time-format") final String timeFormat,
                                                                                  @RequestParam(name = "header-config", required = false) final String customHeaderConfigString,
                                                                                  @RequestParam(name = "buy-value", required = false) final String buyValue,
                                                                                  @RequestParam(name = "csv-separator", required = false) final String csvSeparator,
                                                                                  @RequestParam("dryRun") final boolean dryRun) throws IOException, UnsupportedImportFormatException {
        final SecurityTransactionImportResultDto result = securityImportService.importTransactions(authUser.getId(), accountId, file, getSeparator(csvSeparator), '"', dryRun, customHeaderConfigString, buyValue, timestampFormat, dateFormat, timeFormat);
        return ResponseEntity.ok(result.getLines().reversed());
    }

    private char getSeparator(final String csvSeparator) {
        return StringUtils.hasText(csvSeparator) ? csvSeparator.charAt(0) : ';';
    }
}