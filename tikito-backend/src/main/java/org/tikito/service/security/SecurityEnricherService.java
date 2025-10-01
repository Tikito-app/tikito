package org.tikito.service.security;

import org.tikito.config.TikitoProperties;
import org.tikito.entity.Job;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.repository.IsinRepository;
import org.tikito.repository.JobRepository;
import org.tikito.repository.SecurityRepository;
import org.tikito.service.CacheService;
import org.tikito.service.importer.security.IsinToSymbolConverter;
import org.tikito.service.importer.security.YahooImporter;
import org.tikito.service.job.JobType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SecurityEnricherService {
    private final SecurityRepository securityRepository;
    private final JobRepository jobRepository;
    private final CacheService cacheService;
    private final TikitoProperties properties;
    private final IsinRepository isinRepository;

    public SecurityEnricherService(final SecurityRepository securityRepository,
                                   final JobRepository jobRepository,
                                   final CacheService cacheService,
                                   final TikitoProperties properties,
                                   final IsinRepository isinRepository) {
        this.securityRepository = securityRepository;
        this.jobRepository = jobRepository;
        this.cacheService = cacheService;
        this.properties = properties;
        this.isinRepository = isinRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enrichSecurity(final long securityId) {
        final Security security = securityRepository.findById(securityId).orElseThrow();
        final List<Isin> isins = isinRepository.findBySecurityId(security.getId());

        isins.forEach(isin -> SecurityEnricherService.enrichIsin(security, isin, properties.getFinnhub().getToken()));
        cacheService.refreshSecurities();

        // todo: update security type
        securityRepository.saveAndFlush(security);
        isinRepository.saveAllAndFlush(isins);
        jobRepository.saveAndFlush(Job.create(JobType.UPDATE_SECURITY_PRICES).securityId(securityId).build());
    }

    private static void enrichIsin(final Security security, final Isin isin, final String token) {
        if (!StringUtils.hasText(isin.getSymbol())) {
            enrichSymbol(isin, token);
        }
        YahooImporter.enrichSecurity(security, isin.getIsin(), isin.getSymbol());
    }

    private static void enrichSymbol(final Isin isin, final String token) {
        final Optional<String> symbol = IsinToSymbolConverter.getSymbol(isin.getIsin(), token);
        if (symbol.isEmpty()) {
            log.error("Could not find symbol for isin {}", isin.getIsin());
            return;
        }

        isin.setSymbol(symbol.get());
    }
}
