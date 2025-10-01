package org.tikito.service.importer.security;

import org.tikito.dto.security.SecurityPriceDto;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.exception.ResourceNotFoundException;
import org.tikito.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
public final class YahooImporter {
    public static List<SecurityPriceDto> retrieveHistoricalSecurityPrice(final String symbol,
                                                                         final Long securityId,
                                                                         final LocalDate from,
                                                                         final LocalDate to,
                                                                         final Set<String> processedDates) {
        final String url = getUrlForHistoricalPrices(symbol, from, to);
        final List<SecurityPriceDto> rates = new ArrayList<>();

        try {
            final String json = HttpUtil.downloadUrl(url);
            final JsonNode jsonNode = new ObjectMapper().reader().readTree(json);
            final JsonNode innerNode = jsonNode.get("chart").get("result").get(0);

            if (innerNode == null) {
                return new ArrayList<>();
            }

            final JsonNode timestampNode = innerNode.get("timestamp");
            final JsonNode adjclose = innerNode.get("indicators").get("quote").get(0).get("close");

            for (int i = 0; i < timestampNode.size(); i++) {
                try {
                    final long epoch = timestampNode.get(i).asLong();
                    final double value = adjclose.get(i).asDouble();
                    final Instant timestamp = Instant.ofEpochSecond(epoch);
                    final LocalDate date = LocalDate.ofInstant(timestamp, ZoneOffset.UTC);
                    final String dateString = date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth();

                    if (!processedDates.contains(dateString)) {
                        rates.add(new SecurityPriceDto(securityId, date, value));
                        processedDates.add(dateString);
                    }
                } catch (final Exception e) {
                    log.error("Could not get value", e);
                }
            }
            return rates
                    .stream()
                    .sorted(Comparator.comparing(SecurityPriceDto::getDate))
                    .toList();
        } catch (final ResourceNotFoundException | JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static String getUrlForHistoricalPrices(final String symbol, final LocalDate from, final LocalDate to) {
        final long endEpoch = to.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        final long startEpoch = from.atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        return "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?events=split&formatted=true&includeAdjustedClose=true&interval=1d&period1=" + startEpoch + "&period2=" + endEpoch + "&symbol=" + symbol + "&userYfid=true&lang=en-US&region=US";
    }

    public static void enrichSecurity(final Security security, final String isin, final String symbol) {
        final String searchUrl = "https://query2.finance.yahoo.com/v1/finance/search?q=" + symbol + "&lang=en-US&region=US&quotesCount=6&newsCount=3&listsCount=2&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query&multiQuoteQueryId=multi_quote_single_token_query&newsQueryId=news_cie_vespa&enableCb=false&enableNavLinks=true&enableEnhancedTrivialQuery=true&enableResearchReports=true&enableCulturalAssets=true&enableLogoUrl=true&enableLists=false&recommendCount=5&enablePrivateCompany=true";

        try {
            log.info(searchUrl);
            final String json = HttpUtil.downloadUrl(searchUrl);
            final JsonNode jsonNode = new ObjectMapper().reader().readTree(json);

            if (jsonNode.has("quotes")) {
                final JsonNode quotes = jsonNode.get("quotes");

                if (!quotes.isEmpty()) {
                    final String name = getValueOrNull(quotes, "longname");
                    if (StringUtils.hasText(name)) {
                        security.setName(name);
                    }
                    security.setSector(getValueOrNull(quotes, "sector"));
                    security.setIndustry(getValueOrNull(quotes, "industry"));
                    security.setExchange(getValueOrNull(quotes, "exchange"));

                    final String quoteType = getValueOrNull(quotes, "quoteType");
                    if ("ETF".equals(quoteType)) {
                        security.setSecurityType(SecurityType.ETF);
                    }
                }
            }
        } catch (final ResourceNotFoundException | JsonProcessingException e) {
            log.warn("Cannot retrieve security info for isin {}:", isin, e);
        }
    }
//    https://query1.finance.yahoo.com/v7/finance/quote?fields=fiftyTwoWeekHigh,fiftyTwoWeekLow,fromCurrency,fromExchange,headSymbolAsString,logoUrl,longName,marketCap,messageBoardId,optionsType,overnightMarketTime,overnightMarketPrice,overnightMarketChange,overnightMarketChangePercent,regularMarketTime,regularMarketChange,regularMarketChangePercent,regularMarketOpen,regularMarketPrice,regularMarketSource,regularMarketVolume,postMarketTime,postMarketPrice,postMarketChange,postMarketChangePercent,preMarketTime,preMarketPrice,preMarketChange,preMarketChangePercent,shortName,toCurrency,toExchange,underlyingExchangeSymbol,underlyingSymbol&formatted=true&imgHeights=50&imgLabels=logoUrl&imgWidths=50&symbols=URW.PA&lang=en-AU&region=AU&crumb=.8ihKSax2ZO
//    https://query1.finance.yahoo.com/v7/finance/quote?=&symbols=ING&fields=currency,fromCurrency,toCurrency,exchangeTimezoneName,exchangeTimezoneShortName,gmtOffSetMilliseconds,regularMarketChange,regularMarketChangePercent,regularMarketPrice,regularMarketTime,preMarketChange,preMarketChangePercent,preMarketPrice,preMarketTime,postMarketChange,postMarketChangePercent,postMarketPrice,postMarketTime,extendedMarketChange,extendedMarketChangePercent,extendedMarketPrice,extendedMarketTime,overnightMarketChange,overnightMarketChangePercent,overnightMarketPrice,overnightMarketTime&crumb=.8ihKSax2ZO&formatted=false&region=US&lang=en-US

    private static String getValueOrNull(final JsonNode quotes, final String key) {
        final JsonNode node = quotes.get(0).get(key);
        if (node != null) {
            return node.textValue();
        }
        return null;
    }
}
