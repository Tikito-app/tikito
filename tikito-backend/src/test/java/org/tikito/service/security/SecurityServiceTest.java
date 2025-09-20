package org.tikito.service.security;

import org.tikito.dto.security.SecurityPriceDto;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.SecurityPrice;
import org.tikito.service.BaseIntegrationTest;
import org.tikito.service.importer.security.YahooImporter;
import org.tikito.util.HttpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@Transactional
class SecurityServiceTest extends BaseIntegrationTest {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SecurityEnricherService securityEnricherService;

    @BeforeEach
    public void setup() {
        withDefaultCurrencies();
        withDefaultUserAccount();
        withDefaultAccounts();
        withDefaultCompanies();
    }

    @Test
    void updateSecurityPrices() throws IOException {
        final String json = getClassPathResource("yahoo-wolter-kluwer-rates.json");
        final String yahooUrl = "https://query1.finance.yahoo.com/v8/finance/chart/WKL.AS?events=split&formatted=true&includeAdjustedClose=true&interval=1d&period1=1714176000&period2=1745712000&symbol=WKL.AS&userYfid=true&lang=en-US&region=US";
        try (final MockedStatic<HttpUtil> utilities = Mockito.mockStatic(HttpUtil.class)) {
            utilities.when(() -> HttpUtil.downloadUrl(eq(yahooUrl))).thenAnswer(invocationOnMock -> json);

            final List<SecurityPriceDto> rates = SecurityService.getExchangeRateHistory("WKL.AS", WOLTER_KLUWER.getId(), ONE_YEAR_AGO, NOW, new HashSet<>());
            assertEquals(254, rates.size());
        }
    }

    @Test
    void testFillInTheBlanks() {
        final LocalDate isinValidTo = WOLTER_KLUWER.getIsins().getFirst().getValidTo();
        final List<SecurityPriceDto> firstList = List.of(new SecurityPriceDto(WOLTER_KLUWER.getId(), isinValidTo.minusDays(1), randomDouble(1, 5)));
        final List<SecurityPriceDto> secondList = List.of(new SecurityPriceDto(WOLTER_KLUWER.getId(), isinValidTo.plusDays(2), randomDouble(1, 5)));

        try (final MockedStatic<YahooImporter> yahooImporter = Mockito.mockStatic(YahooImporter.class)) {
            yahooImporter.when(() -> YahooImporter.retrieveHistoricalSecurityPrice(
                    eq("WKL.AS"),
                    eq(WOLTER_KLUWER.getId()),
                    any(),
                    eq(isinValidTo),
                    anySet())).thenAnswer(invocationOnMock -> firstList);
            yahooImporter.when(() -> YahooImporter.retrieveHistoricalSecurityPrice(
                    eq("WKL.AS"),
                    eq(WOLTER_KLUWER.getId()),
                    eq(isinValidTo.plusDays(1)),
                    any(),
                    anySet())).thenAnswer(invocationOnMock -> secondList);

            securityService.updateSecurityPrices(WOLTER_KLUWER.getId());

            final List<SecurityPrice> prices = securityPriceRepository.findAllBySecurityId(WOLTER_KLUWER.getId());

            assertEquals(4, prices.size());
            assertEquals(isinValidTo.minusDays(1), prices.get(0).getDate());
            assertEquals(isinValidTo, prices.get(1).getDate());
            assertEquals(isinValidTo.plusDays(1), prices.get(2).getDate());
            assertEquals(isinValidTo.plusDays(2), prices.get(3).getDate());
        }
    }

    @Test
    void testEnrichSecurityMore() throws IOException {
        final String json = getClassPathResource("yahoo-wolter-kluwer-quote.json");
        final Isin isin = WOLTER_KLUWER.getLatestIsin();
        final String yahooUrl = "https://query2.finance.yahoo.com/v1/finance/search?q=" +isin.getSymbol() + "&lang=en-US&region=US&quotesCount=6&newsCount=3&listsCount=2&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&newsQueryId=news_cie_vespa&enableCb=false&enableNavLinks=true&enableEnhancedTrivialQuery=true&enableResearchReports=true&enableCulturalAssets=true&enableLogoUrl=true&enableLists=false&recommendCount=5&enablePrivateCompany=true";
        WOLTER_KLUWER.setName("");
        securityRepository.save(WOLTER_KLUWER);

        try (final MockedStatic<HttpUtil> utilities = Mockito.mockStatic(HttpUtil.class)) {
            utilities.when(() -> HttpUtil.downloadUrl(eq(yahooUrl))).thenAnswer(invocationOnMock -> json);

            // todo
            securityEnricherService.enrichSecurity(WOLTER_KLUWER.getId());

            assertEquals("Wolters Kluwer N.V.", WOLTER_KLUWER.getName());
            assertEquals("WKL.AS", isin.getSymbol());
            assertEquals("AMS", WOLTER_KLUWER.getExchange());
            assertEquals("Industrials", WOLTER_KLUWER.getSector());
            assertEquals("Specialty Business Services", WOLTER_KLUWER.getIndustry());
        }
    }
}