package org.tikito.service.importer.security;

import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.tikito.dto.security.SecurityTransactionImportResultDto.*;
import static org.tikito.dto.security.SecurityTransactionType.*;

@Service
public class DeGiroAccountImporter extends SecurityTransactionImporter {
    private static final List<String> DEGIRO_HEADERS = Stream.of(
                    "Datum",
                    "Tijd",
                    "Valutadatum",
                    "Product",
                    "ISIN",
                    "Omschrijving",
                    "FX",
                    "Mutatie",
                    null,
                    "Saldo",
                    null,
                    "Order Id")
            .toList();

    private static final String CURRENY_REGEX = "(.*)";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Pattern buySellPattern = Pattern.compile("(Koop|Verkoop) ([0-9]+) @ " + CURRENY_REGEX + " ([A-Z]+)");
    private static final Pattern buySellNewIsinPattern = Pattern.compile("WIJZIGING ISIN: (Koop|Verkoop) ([0-9]+) @ " + CURRENY_REGEX + " ([A-Z]+)");
    private static final Pattern buySellProductChangePattern = Pattern.compile("PRODUCTWIJZIGING : (Koop|Verkoop) ([0-9]+) @ " + CURRENY_REGEX + " ([A-Z]+)");
    private static final Pattern taxForCountryPattern = Pattern.compile("Transactiebelasting (.*)");
    private static final Pattern transferFromPattern = Pattern.compile("Overboeking van uw geldrekening bij flatexDEGIRO Bank " + CURRENY_REGEX + " ([A-Z]+)");
    private static final Pattern transferToPattern = Pattern.compile("Overboeking naar uw geldrekening bij flatexDEGIRO Bank: " + CURRENY_REGEX + " ([A-Z]+)");
    private static final Pattern adminCostsPattern = Pattern.compile("DEGIRO Aansluitingskosten (.*)");

    private final Map<String, SecurityTransactionType> descriptionToTypeMap = new HashMap<>();


    public DeGiroAccountImporter() {
        descriptionToTypeMap.put("Degiro Cash Sweep Transfer", CASH_SWEEP);
        descriptionToTypeMap.put("DEGIRO Transactiekosten en/of kosten van derden", THIRD_PARTY_COSTS);
        descriptionToTypeMap.put("Dividend", DIVIDEND);
        descriptionToTypeMap.put("Kapitaalsuitkering", DIVIDEND);
        descriptionToTypeMap.put("Dividendbelasting", DIVIDEND_TAX);
        descriptionToTypeMap.put("Flatex Interest", INTEREST);
        descriptionToTypeMap.put("Flatex Interest Income", INTEREST);
        descriptionToTypeMap.put("flatex terugstorting", FLATEX_DEPOSIT);
        descriptionToTypeMap.put("iDEAL Deposit", IDEAL_DEPOSIT);
        descriptionToTypeMap.put("iDEAL storting", IDEAL_DEPOSIT);
        descriptionToTypeMap.put("Processed Flatex Withdrawal", WITHDRAWAL);
        descriptionToTypeMap.put("Reservation iDEAL / Sofort Deposit", IDEAL_RESERVATION);
        descriptionToTypeMap.put("Valuta Creditering", CURRENCY_CREDITING);
        descriptionToTypeMap.put("Valuta Debitering", CURRENCY_DEBITING);
        descriptionToTypeMap.put("Minimale activiteitskosten pensioenrekening", ADMIN_COSTS);
        descriptionToTypeMap.put("Teruggave NL Dividend Tax", DIVIDEND_TAX);
    }

    @Override
    public List<String> getHeaders() {
        return DEGIRO_HEADERS;
    }

    @Override
    public List<SecurityTransactionImportLine> map(final SecurityTransactionImportLine line) {
        final List<SecurityTransactionImportLine> lines = new ArrayList<>();
        lines.add(line);

        if (line.getCells().size() < 11) {
            line.setFailedReason(FAILED_INVALID_LINE);
            return lines;
        }

        final String description = line.getCells().get(5);
        SecurityTransactionType transactionType = null;
        int amount = 0;
        double price = 0;
        String currency = null;
        String country = null;
        Matcher matcher;
        final Instant timestamp;

        try {
            final LocalDate date = LocalDate.parse(line.getCells().get(0), dateFormatter);
            final LocalTime time = LocalTime.parse(line.getCells().get(1));
            timestamp = LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC);
        } catch (final Exception e) {
            line.setFailedReason(FAILED_NO_VALID_TIMESTAMP);
            return lines;
        }


        try {
            if (descriptionToTypeMap.containsKey(description)) {
                transactionType = descriptionToTypeMap.get(description);
            } else if ((matcher = matches(taxForCountryPattern, description, 1)) != null) {
                transactionType = COUNTRY_TAX;
                country = matcher.group(1);
            } else if ((matcher = matches(buySellPattern, description, 4)) != null) {
                transactionType = "koop".equalsIgnoreCase(matcher.group(1)) ? BUY : SELL;
                amount = Integer.parseInt(matcher.group(2));
                price = parsePrice(matcher.group(3));
                currency = matcher.group(4);
            } else if ((matcher = matches(buySellNewIsinPattern, description, 4)) != null) {
                transactionType = "koop".equalsIgnoreCase(matcher.group(1)) ? BUY_ISIN_CHANGE : SELL_ISIN_CHANGE;
                amount = Integer.parseInt(matcher.group(2));
                price = parsePrice(matcher.group(3));
                currency = matcher.group(4);
            } else if ((matcher = matches(buySellProductChangePattern, description, 4)) != null) {
                transactionType = "koop".equalsIgnoreCase(matcher.group(1)) ? BUY_PRODUCT_CHANGE : SELL_PRODUCT_CHANGE;
                amount = Integer.parseInt(matcher.group(2));
                price = parsePrice(matcher.group(3));
                currency = matcher.group(4);
            } else if ((matcher = matches(transferFromPattern, description, 2)) != null) {
                transactionType = TRANSFER_FROM;
                price = parsePrice(matcher.group(1));
                currency = matcher.group(2);
            } else if ((matcher = matches(transferToPattern, description, 2)) != null) {
                transactionType = TRANSFER_TO;
                price = parsePrice(matcher.group(1));
                currency = matcher.group(2);
            } else if ((matcher = matches(adminCostsPattern, description, 1)) != null) {
                transactionType = ADMIN_COSTS;
            }

            if (!StringUtils.hasText(currency)) {
                currency = line.getCells().get(9);
            }

            if (transactionType == null) {
                line.setFailedReason(FAILED_NO_TRANSACTION_TYPE);
                return lines;
            }

            if (price == 0 && transactionType != INTEREST) {
                try {
                    price = parsePrice(line.getCells().get(8));
                    if (price == 0) {
                        line.setFailedReason(FAILED_NO_PRICE);
                        return lines;
                    }
                } catch (final Exception e) {
                    line.setFailedReason(FAILED_NO_PRICE);
                    return lines;
                }
            }

            if (transactionType == BUY || transactionType == BUY_ISIN_CHANGE || transactionType == BUY_PRODUCT_CHANGE) {
                price = -Math.abs(price);
            } else if (transactionType == SELL || transactionType == SELL_ISIN_CHANGE || transactionType == SELL_PRODUCT_CHANGE) {
                price = Math.abs(price);
            }

            line.setTimestamp(timestamp);
            line.setIsin(line.getCells().get(4));
            line.setDescription(line.getCells().get(5));
            line.setProductName(line.getCells().get(3));
            line.setAmount(amount);
            line.setPrice(price);
            line.setTransactionType(transactionType);
            line.setCurrency(currency);
            line.setCountry(country);
            line.setCash(parsePrice(line.getCells().get(10)));
        } catch (final Exception e) {
            line.setFailedReason(e.getMessage());
        }

        return lines;
    }

    private double parsePrice(final String price) {
        if (price.contains(",") && price.contains(".")) {
            return Double.parseDouble(price.replaceAll("\\.", "").replaceAll(",", "."));
        } else if (price.contains(".")) {
            // nasty code for a price such as 4.300
            final int index = price.indexOf(".");
            final String pricePart = price.substring(index + 1);
            if (price.endsWith("00") && pricePart.length() > 2) {
                return Double.parseDouble(price.replace(".", ""));
            }
        }
        return Double.parseDouble(price.replaceAll(",", "."));
    }

    private Matcher matches(final Pattern pattern, final String description, final int expectedGroups) {
        final Matcher matcher = pattern.matcher(description);
        if (matcher.matches() && matcher.groupCount() == expectedGroups) {
            return matcher;
        }
        return null;
    }
}
