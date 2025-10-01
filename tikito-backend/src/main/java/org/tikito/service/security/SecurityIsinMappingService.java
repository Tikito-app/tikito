package org.tikito.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tikito.dto.AccountDto;
import org.tikito.dto.security.*;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.repository.IsinRepository;
import org.tikito.repository.SecurityRepository;
import org.tikito.service.CacheService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tikito.dto.security.SecurityTransactionImportResultDto.*;

@Service
@Slf4j
public class SecurityIsinMappingService {
    private final CacheService cacheService;
    private final IsinRepository isinRepository;
    private final SecurityRepository securityRepository;

    public SecurityIsinMappingService(final CacheService cacheService, final IsinRepository isinRepository, final SecurityRepository securityRepository) {
        this.cacheService = cacheService;
        this.isinRepository = isinRepository;
        this.securityRepository = securityRepository;
    }

    public void enrichValidateAndMap(final AccountDto account, final SecurityTransactionImportResultDto result) {
        final Set<String> isinSet = filterNonFailed(result).map(SecurityTransactionImportLine::getIsin).collect(Collectors.toSet());
        final Map<String, Isin> knownIsins = new HashMap<>();

        isinRepository.findAllById(isinSet).forEach(isin -> knownIsins.put(isin.getIsin(), isin));

        final Map<Long, Security> knownSecuritiesById = securityRepository.findAllById(knownIsins.values().stream().map(Isin::getSecurityId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Security::getId, Function.identity()));
        final Map<String, Security> knownSecuritiesByIsin = new HashMap<>();

        knownIsins.values().forEach(isin -> {
            knownSecuritiesByIsin.put(isin.getIsin(), knownSecuritiesById.get(isin.getSecurityId()));
        });

        // todo: create new job to enrich the trading companies
        final List<SecurityTransactionImportLine> lines = filterNonFailed(result).toList();

        for (int i = 0; i < lines.size(); i++) {
            enrichValidateAndMap(account,
                    knownIsins,
                    result.getNewIsinsByIsin(),
                    knownSecuritiesByIsin,
                    result.getNewSecuritiesByIsin(),
                    lines,
                    i);
        }
    }

    /**
     * Validates for valid currency. In case an isin is present,
     */
    void enrichValidateAndMap(final AccountDto account,
                              final Map<String, Isin> knownIsins,
                              final Map<String, Isin> newIsinsByIsin,
                              final Map<String, Security> knownSecuritiesByIsin,
                              final Map<String, Security> newSecuritiesByIsin,
                              final List<SecurityTransactionImportLine> lines,
                              final int currentIndex) {
        final SecurityTransactionImportLine line = lines.get(currentIndex);
        if (line.isFailed()) {
            return;
        }

        enrichValidateAndMapExchangeRate(line);
        enrichValidateAndMapCurrency(account, line);

        if (line.isFailed()) {
            return;
        }

        if (!StringUtils.hasText(line.getIsin())) {
            return;
        }

        enrichValidateAndMapIsin(line, knownIsins, newIsinsByIsin, knownSecuritiesByIsin, newSecuritiesByIsin, lines, currentIndex);
    }

    /**
     * Will either process an isin change, or add the unknown isin and security
     */
    private void enrichValidateAndMapIsin(final SecurityTransactionImportLine line,
                                          final Map<String, Isin> knownIsins,
                                          final Map<String, Isin> newIsinsByIsin,
                                          final Map<String, Security> knownSecuritiesByIsin,
                                          final Map<String, Security> newSecuritiesByIsin,
                                          final List<SecurityTransactionImportLine> lines,
                                          final int currentIndex) {
        if (line.getTransactionType() == SecurityTransactionType.BUY_ISIN_CHANGE) {
            enrichValidateAndMapIsinChange(line, knownIsins, newIsinsByIsin, knownSecuritiesByIsin, newSecuritiesByIsin, lines, currentIndex);
        } else if (!knownIsins.containsKey(line.getIsin())) {
            enrichValidateAndMapNewIsin(line, knownIsins, newIsinsByIsin, knownSecuritiesByIsin, newSecuritiesByIsin);
        } else {
            line.setSecurity(knownSecuritiesByIsin.get(line.getIsin()));
        }
    }

    /**
     * Validates that it is really an isin change and if so, it will update the valid to date in the old isin and add
     * the new isin to the maps, plus the securities by isin map.
     */
    private void enrichValidateAndMapIsinChange(final SecurityTransactionImportLine line,
                                                final Map<String, Isin> knownIsins,
                                                final Map<String, Isin> newIsinsByIsin,
                                                final Map<String, Security> knownSecuritiesByIsin,
                                                final Map<String, Security> newSecuritiesByIsin,
                                                final List<SecurityTransactionImportLine> lines,
                                                final int currentIndex) {
        // in this case, the new isin is on the next line, so we need to validate that the next line exists
        if (currentIndex == 0) {
            line.setFailedReason(FAILED_EXPECTED_SELL_ISIN_CHANGE);
            return;
        }

        final SecurityTransactionImportLine previousLine = lines.get(currentIndex - 1);
        final Isin oldIsin = knownIsins.get(previousLine.getIsin());

        if (previousLine.getTransactionType() != SecurityTransactionType.SELL_ISIN_CHANGE || !knownSecuritiesByIsin.containsKey(oldIsin.getIsin())) {
            line.setFailedReason(FAILED_EXPECTED_SELL_ISIN_CHANGE);
            return;
        } else if (!line.getTimestamp().equals(previousLine.getTimestamp())) {
            line.setFailedReason(FAILED_EXPECTED_BUY_NEW_ISIN_SAME_TIMESTAMP);
            return;
        }

        final Security security = knownSecuritiesByIsin.get(oldIsin.getIsin());

        if (!knownIsins.containsKey(line.getIsin())) {
            final LocalDate date = LocalDate.ofInstant(line.getTimestamp(), ZoneOffset.UTC);
            oldIsin.setValidTo(date.minusDays(1));

            final Isin newIsin = new Isin(line.getIsin());
            newIsin.setValidFrom(date);
            knownIsins.put(line.getIsin(), newIsin);
            newIsinsByIsin.put(line.getIsin(), newIsin);
            newSecuritiesByIsin.put(line.getIsin(), security);
            knownSecuritiesByIsin.put(line.getIsin(), security);
        }

        security.setCurrentIsin(line.getIsin());
        newSecuritiesByIsin.put(line.getIsin(), security); // make sure it is updated
        line.setSecurity(security);
    }

    private void enrichValidateAndMapNewIsin(final SecurityTransactionImportLine line,
                                             final Map<String, Isin> knownIsins,
                                             final Map<String, Isin> newIsinsByIsin,
                                             final Map<String, Security> knownSecuritiesByIsin,
                                             final Map<String, Security> newSecuritiesByIsin) {
        log.warn("Accepting unknown isin {}", line.getIsin());
        final Isin isin = new Isin(line.getIsin());
        final Security security = new Security();

        security.setSecurityType(SecurityType.STOCK);
        security.setName(line.getProductName());
        security.setCurrencyId(line.getCurrencyId());
        security.setCurrentIsin(line.getIsin());
        if (!StringUtils.hasText(security.getName())) {
            security.setName(line.getIsin());
        }

        newSecuritiesByIsin.put(line.getIsin(), security);
        newIsinsByIsin.put(line.getIsin(), isin);

        knownIsins.put(line.getIsin(), isin);
        knownSecuritiesByIsin.put(line.getIsin(), security);
        line.setSecurity(security);
    }

    private void enrichValidateAndMapExchangeRate(final SecurityTransactionImportLine line) {
        if (line.getExchangeRate() == null) {
            line.setExchangeRate(cacheService.getCurrencyMultiplier(line.getCurrencyId(), LocalDate.ofInstant(line.getTimestamp(), ZoneOffset.UTC)));
        }

        if (line.getExchangeRate() == null) {
            line.setFailedReason(FAILED_NO_EXCHANGE_RATE);
            return;
        }
    }

    private void enrichValidateAndMapCurrency(final AccountDto account, final SecurityTransactionImportLine line) {
        if (StringUtils.hasText(line.getCurrency())) {
            final Optional<SecurityDto> currency = cacheService.getCurrency(line.getCurrency(), line.getTimestamp());
            if (currency.isEmpty()) {
                line.setFailedReason(FAILED_NO_KNOWN_CURRENCY);
                return;
            }
            line.setCurrencyId(currency.get().getId());
        } else {
            line.setCurrencyId(account.getCurrencyId());
        }
    }

    private Stream<SecurityTransactionImportLine> filterNonFailed(final SecurityTransactionImportResultDto result) {
        return result
                .getLines()
                .stream()
                .filter(line -> !line.isFailed());
    }
}
