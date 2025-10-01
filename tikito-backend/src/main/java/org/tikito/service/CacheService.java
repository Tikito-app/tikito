package org.tikito.service;

import org.springframework.stereotype.Service;
import org.tikito.dto.security.IsinDto;
import org.tikito.dto.security.SecurityDto;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.security.Security;
import org.tikito.repository.IsinRepository;
import org.tikito.repository.SecurityPriceRepository;
import org.tikito.repository.SecurityRepository;
import org.tikito.repository.UserAccountRepository;
import org.tikito.service.security.IsinHelper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CacheService {
    private final SecurityRepository securityRepository;
    private final IsinRepository isinRepository;
    private final SecurityPriceRepository securityPriceRepository;
    private final UserAccountRepository userAccountRepository;

    private Map<Long, SecurityDto> securitiesById = new HashMap<>();
    private Map<Long, List<IsinDto>> isinsBySecurityId = new HashMap<>();
    private final Map<Long, Map<LocalDate, Double>> currencyToEuroMultiplier = new HashMap<>();
    private final List<SecurityDto> currencies = new ArrayList<>();
    private Boolean firstEverUser;

    public CacheService(final SecurityRepository securityRepository,
                        final IsinRepository isinRepository,
                        final SecurityPriceRepository securityPriceRepository,
                        final UserAccountRepository userAccountRepository) {
        this.securityRepository = securityRepository;
        this.isinRepository = isinRepository;
        this.securityPriceRepository = securityPriceRepository;
        this.userAccountRepository = userAccountRepository;

        refreshCurrencies();
        refreshFirstEverUser();
    }

    public void refreshFirstEverUser() {
        firstEverUser = userAccountRepository.count() == 0;
    }

    public void refreshSecurities() {
        this.securitiesById = securityRepository
                .findAll()
                .stream()
                .map(Security::toDto)
                .collect(Collectors.toMap(SecurityDto::getId, Function.identity()));
        isinsBySecurityId = new HashMap<>();
        isinRepository
                .findAll()
                .forEach(isin -> {
                    isinsBySecurityId.putIfAbsent(isin.getSecurityId(), new ArrayList<>());
                    isinsBySecurityId.get(isin.getSecurityId()).add(isin.toDto());
                });
    }

    public void refreshCurrencies() {
        refreshSecurities();
        currencies.clear();
        currencies.addAll(securityRepository
                .findBySecurityType(SecurityType.CURRENCY)
                .stream()
                .map(Security::toDto)
                .toList());

        final Set<Long> currencyIds = currencies
                .stream()
                .map(SecurityDto::getId)
                .collect(Collectors.toSet());
        currencyIds.forEach(id -> currencyToEuroMultiplier.put(id, new HashMap<>()));
        securityPriceRepository
                .findAllBySecurityIdIn(currencyIds)
                .forEach(price ->
                        currencyToEuroMultiplier.get(price.getSecurityId())
                                .put(price.getDate(), price.getPrice()));

    }

    public Optional<SecurityDto> getCurrency(final String isin, final Instant timestamp) {
        final LocalDate date = timestamp == null ? null : LocalDate.ofInstant(timestamp, ZoneOffset.UTC);
        return currencies
                .stream()
                .filter(currency ->
                        isinsBySecurityId
                                .get(currency.getId())
                                .stream()
                                .anyMatch(isinDto -> isinDto.getIsin().equals(isin) && IsinHelper.isValid(isinDto, date, null)))
                .findFirst();
    }

    public SecurityDto getSecurity(final long securityId) {
        return securitiesById.get(securityId);
    }

    public double getCurrencyMultiplier(final long currencyId, final LocalDate date) {
        final Map<LocalDate, Double> pricePerDate = currencyToEuroMultiplier.get(currencyId);
        if (pricePerDate == null) {
            return 1;
        }
        return pricePerDate.getOrDefault(date, 1.0);
    }

    public boolean isFirstEverUser() {
        return firstEverUser;
    }

    public void firstEverUserRegistered() {
        firstEverUser = false;
    }
}
