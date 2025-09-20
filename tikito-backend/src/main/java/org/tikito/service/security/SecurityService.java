package org.tikito.service.security;

import org.springframework.util.StringUtils;
import org.tikito.dto.IsinDto;
import org.tikito.dto.security.SecurityDto;
import org.tikito.dto.security.SecurityPriceDto;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.Job;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.entity.security.SecurityPrice;
import org.tikito.repository.IsinRepository;
import org.tikito.repository.SecurityPriceRepository;
import org.tikito.repository.SecurityRepository;
import org.tikito.service.importer.security.YahooImporter;
import org.tikito.service.job.JobProcessor;
import org.tikito.service.job.JobType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SecurityService implements JobProcessor {
    private final SecurityRepository securityRepository;
    private final SecurityPriceRepository securityPriceRepository;
    private final SecurityEnricherService securityEnricherService;
    private final IsinRepository isinRepository;

    public SecurityService(final SecurityRepository securityRepository,
                           final SecurityPriceRepository securityPriceRepository,
                           final SecurityEnricherService securityEnricherService,
                           final IsinRepository isinRepository) {
        this.securityRepository = securityRepository;
        this.securityPriceRepository = securityPriceRepository;
        this.securityEnricherService = securityEnricherService;
        this.isinRepository = isinRepository;
    }

    public SecurityDto getSecurity(final long id) {
        return securityRepository
                .findById(id)
                .map(Security::toDto)
                .orElseThrow();
    }

    public List<SecurityDto> getSecurities() {
        return securityRepository
                .findAll()
                .stream()
                .map(Security::toDto)
//                .filter(security -> SecurityType.CURRENCY != security.getSecurityType())
                .sorted(Comparator.comparing(SecurityDto::getName))
                .toList();
    }

    public List<SecurityDto> getSecurities(final SecurityType type) {
        return securityRepository
                .findAll()
                .stream()
                .filter(security -> type != null && type == security.getSecurityType())
                .map(Security::toDto)
                .toList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateSecurityPrices(final long securityId) {
        final SecurityDto security = getSecurity(securityId);
        final Set<String> processedDates = securityPriceRepository
                .findAllBySecurityId(securityId)
                .stream()
                .map(price -> price.getDate().getYear() + "-" + price.getDate().getMonthValue() + "-" + price.getDate().getDayOfMonth())
                .collect(Collectors.toSet());
        final List<SecurityPriceDto> exchangeRateHistory = new ArrayList<>();
        final Optional<LocalDate> latestPrice = securityPriceRepository.findDateOfLatestPrice(securityId);
        final LocalDate initialStartDate = latestPrice.orElse(
                LocalDate.now().minusYears(10));
        final LocalDate now = LocalDate.now();
        LocalDate startDate = initialStartDate;
        boolean hasSetDateFromIsin = false;

        while (startDate.isBefore(now)) {
            final Optional<IsinDto> optionalIsin = security.getIsin(startDate, null);

            if (optionalIsin.isEmpty()) {
                log.error("No isin for security {} at {}", security.getId(), startDate);
                return;
            } else if (!StringUtils.hasText(optionalIsin.get().getSymbol())) {
                log.error("No symbol for security {} at {}", security.getId(), startDate);
                return;
            }

            final IsinDto isin = optionalIsin.get();
            final LocalDate isinEndDate = isin.getValidTo();

            LocalDate endDate = startDate.plusDays(365);
            if (isinEndDate != null && endDate.isAfter(isinEndDate)) {
                endDate = isinEndDate;
                hasSetDateFromIsin = true;
            }

            endDate = endDate.isAfter(now) ? now : endDate;

            try {
                final List<SecurityPriceDto> foundPrices = getExchangeRateHistory(
                        isin.getSymbol(),
                        securityId,
                        startDate,
                        endDate,
                        processedDates);
                // if we don't have any price yet, keep looking, cause the security might not have been created in this time
                if (!foundPrices.isEmpty()) {
                    exchangeRateHistory.addAll(foundPrices);
                    try {
                        Thread.sleep(200);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (final RuntimeException e) {
                log.error("Could not fetch exchange rate for security {}", securityId, e);
                return; // return, because we cannot have missing prices, or else the fill in the blanks would not work
            }

            startDate = endDate;
            if (hasSetDateFromIsin) {
                startDate = startDate.plusDays(1);
                hasSetDateFromIsin = false;
            }
        }

        fillInTheGaps(exchangeRateHistory);

        securityPriceRepository.saveAllAndFlush(
                exchangeRateHistory
                        .stream()
                        .map(SecurityPrice::new)
                        .toList());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deletePrices(final long securityId) {
        securityPriceRepository.deleteAllBySecurityId(securityId);
    }

    private static void fillInTheGaps(final List<SecurityPriceDto> exchangeRateHistory) {
        if (exchangeRateHistory.size() <= 1) {
            return;
        }

        final List<SecurityPriceDto> interpolatedSecurityPrices = new ArrayList<>();
        final LocalDate startDate = exchangeRateHistory.getFirst().getDate();
        final LocalDate endDate = exchangeRateHistory.getLast().getDate();
        final Map<LocalDate, SecurityPriceDto> pricesPerDate = exchangeRateHistory
                .stream()
                .collect(Collectors.toMap(SecurityPriceDto::getDate, Function.identity()));

        LocalDate currentDate = startDate;
        SecurityPriceDto lastPriceDto = exchangeRateHistory.getFirst();
        while (currentDate.isBefore(endDate)) {
            if (!pricesPerDate.containsKey(currentDate)) {
                final SecurityPriceDto dto = new SecurityPriceDto(
                        lastPriceDto.getSecurityId(),
                        currentDate,
                        lastPriceDto.getPrice());
                interpolatedSecurityPrices.add(dto);
            } else {
                lastPriceDto = pricesPerDate.get(currentDate);
            }

            currentDate = currentDate.plusDays(1);
        }

        exchangeRateHistory.addAll(interpolatedSecurityPrices);
        exchangeRateHistory.sort(Comparator.comparing(SecurityPriceDto::getDate));
    }

    public static List<SecurityPriceDto> getExchangeRateHistory(final String symbol,
                                                                final Long securityId,
                                                                final LocalDate from,
                                                                final LocalDate to,
                                                                final Set<String> foundDates) {
        return YahooImporter.retrieveHistoricalSecurityPrice(symbol, securityId, from, to, foundDates);
    }

    public List<SecurityPriceDto> getPrices(final Long id) {
        return securityPriceRepository
                .findAllBySecurityId(id)
                .stream()
                .map(SecurityPrice::toDto)
                .sorted(Comparator.comparing(SecurityPriceDto::getDate))
                .toList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SecurityDto editSecurity(final long securityId, final String name) {
        final Security security = securityRepository.findById(securityId).orElseThrow();
        security.setName(name);
        return security.toDto();
    }

    public IsinDto getIsin(final String isin) {
        return isinRepository
                .findById(isin)
                .orElseThrow()
                .toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public IsinDto updateIsin(final String isin, final String symbol, final LocalDate validFrom, final LocalDate validTo) {
        final Isin entity = isinRepository.findById(isin).orElseThrow();
        entity.setSymbol(symbol);
        entity.setValidFrom(validFrom);
        entity.setValidTo(validTo);
        return isinRepository.saveAndFlush(entity).toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteIsin(final String isin) {
        isinRepository.deleteById(isin);
    }

    @Override
    public boolean canProcess(final Job job) {
        return job.getJobType() == JobType.UPDATE_SECURITY_PRICES ||
                job.getJobType() == JobType.DELETE_SECURITY_PRICES ||
                job.getJobType() == JobType.ENRICH_SECURITY;
    }

    @Override
    public void process(final Job job) {
        switch (job.getJobType()) {
            case UPDATE_SECURITY_PRICES -> updateSecurityPrices(job.getSecurityId());
            case DELETE_SECURITY_PRICES -> deletePrices(job.getSecurityId());
            case ENRICH_SECURITY -> securityEnricherService.enrichSecurity(job.getSecurityId());
            default -> throw new IllegalStateException();
        }
    }
}
