package org.tikito.service.importer.security;

import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionImportResultDto;
import org.tikito.dto.security.SecurityTransactionType;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.tikito.dto.security.SecurityTransactionImportResultDto.FAILED_NO_VALID_TIMESTAMP;
import static org.tikito.service.importer.security.CustomSecurityHeaderName.*;

public class CustomSecurityTransactionImporter extends SecurityTransactionImporter {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Map<String, Integer> headerConfig;
    private final String buyValue;
    private final String timestampFormat;
    private final String dateFormat;
    private final String timeFormat;

    public CustomSecurityTransactionImporter(final Map<String, Integer> headerConfig, final String buyValue, final String timestampFormat, final String dateFormat, final String timeFormat) {
        this.headerConfig = headerConfig;
        this.buyValue = buyValue;
        this.timestampFormat = timestampFormat;
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
    }

    @Override
    protected List<String> getHeaders() {
        return List.of(); // not needed
    }

    @Override
    public List<SecurityTransactionImportLine> map(final SecurityTransactionImportLine line) {
        final List<SecurityTransactionImportLine> result = new ArrayList<>();


        mapDateTime(line);
        line.setIsin(getValue(ISIN, line));
        line.setAmount(getIntValue(AMOUNT, line, SecurityTransactionImportResultDto.FAILED_NO_AMOUNT));
        line.setPrice(getDoubleValue(PRICE, line, SecurityTransactionImportResultDto.FAILED_NO_PRICE));
        line.setDescription(getValue(DESCRIPTION, line));
        extractCurrency(line);
        applyBuySell(line);

        if (StringUtils.hasText(line.getIsin()) || line.getAmount() != 0 || line.getPrice() != 0) {
            result.add(line);
        }

        if (hasValue(TRANSACTION_COST, line)) {
            final SecurityTransactionImportLine extraLine = copy(line);
            extraLine.setTransactionType(SecurityTransactionType.TRANSACTION_COST);
            extraLine.setPrice(getDoubleValue(TRANSACTION_COST, line, SecurityTransactionImportResultDto.FAILED_NO_TRANSACTION_COST));
            result.add(extraLine);
            extractCurrency(extraLine);
        }

        if (hasValue(ADMIN_COST, line)) {
            final SecurityTransactionImportLine extraLine = copy(line);
            extraLine.setTransactionType(SecurityTransactionType.ADMIN_COSTS);
            extraLine.setPrice(getDoubleValue(ADMIN_COST, line, SecurityTransactionImportResultDto.FAILED_NO_ADMIN_COST));
            result.add(extraLine);
            extractCurrency(extraLine);
        }

        return result;
    }

    private SecurityTransactionImportLine copy(final SecurityTransactionImportLine line) {
        final SecurityTransactionImportLine newLine = new SecurityTransactionImportLine();
        newLine.setCells(new ArrayList<>(line.getCells()));
        newLine.setLineNumber(line.getLineNumber());
        newLine.setTimestamp(line.getTimestamp());
        newLine.setCurrency(line.getCurrency());
        newLine.setIsin(line.getIsin());
        return newLine;
    }

    private void applyBuySell(final SecurityTransactionImportLine line) {
        if (StringUtils.hasText(buyValue) && hasValue(BUY_SELL, line)) {
            line.setTransactionType(buyValue.equals(getValue(BUY_SELL, line)) ? SecurityTransactionType.BUY : SecurityTransactionType.SELL);
        } else {
            if (line.getPrice() < 0) {
                line.setTransactionType(SecurityTransactionType.SELL);
            } else {
                line.setTransactionType(SecurityTransactionType.BUY);
            }
        }

        if ((line.getTransactionType() == SecurityTransactionType.SELL && line.getPrice() < 0) ||
                (line.getTransactionType() == SecurityTransactionType.BUY && line.getPrice() > 0)) {
            line.setPrice(-line.getPrice());
        }
    }

    private void extractCurrency(final SecurityTransactionImportLine line) {
        if (hasValue(CURRENCY, line)) {
            line.setCurrency(getValue(CURRENCY, line));
        }
        if (hasValue(EXCHANGE_RATE, line)) {
            line.setExchangeRate(getDoubleValue(EXCHANGE_RATE, line, SecurityTransactionImportResultDto.FAILED_NO_EXCHANGE_RATE));
        }
    }


    private void mapDateTime(final SecurityTransactionImportLine line) {
        final String dateString = getValue(DATE, line);
        final String timeString = getValue(TIME, line);

        try {
            final LocalDate date = LocalDate.parse(dateString, dateFormatter);
            if (timeString == null) {
                line.setTimestamp(date.atStartOfDay().toInstant(ZoneOffset.UTC));
            } else {
                final LocalTime time = LocalTime.parse(timeString);
                line.setTimestamp(LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC));
            }
        } catch (final Exception e) {
            line.setFailedReason(FAILED_NO_VALID_TIMESTAMP);
        }
    }

    private boolean hasValue(final String field, final SecurityTransactionImportLine line) {
        return StringUtils.hasText(getValue(field, line));
    }

    private String getValue(final String field, final SecurityTransactionImportLine line) {
        if (headerConfig.get(field) == null) {
            return null;
        }
        return line.getCells().get(headerConfig.get(field));
    }

    private int getIntValue(final String field, final SecurityTransactionImportLine line, final String errorMessage) {
        final String value = getValue(field, line);
        try {
            return Integer.parseInt(value);
        } catch (final Exception e) {
            line.setFailedReason(errorMessage);
            return 0;
        }
    }

    private double getDoubleValue(final String field, final SecurityTransactionImportLine line, final String errorMessage) {
        final String value = getValue(field, line);
        try {
            return Double.parseDouble(value);
        } catch (final Exception e) {
            line.setFailedReason(errorMessage);
            return 0;
        }
    }
}
