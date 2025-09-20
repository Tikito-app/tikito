package org.tikito.service.money;

import org.tikito.dto.DebitCredit;
import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.service.importer.money.MoneyTransactionFileParser;
import org.tikito.service.importer.money.MoneyTransactionImportSettings;
import org.tikito.util.Util;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

public final class MoneySettingsService {

    public static MoneyTransactionImportResultDto applySettings(final MoneyTransactionImportSettings settings,
                                                                final List<List<String>> lines,
                                                                final MoneyTransactionFileParser importer,
                                                                final String filename) {
        final MoneyTransactionImportResultDto result = new MoneyTransactionImportResultDto(lines, settings, importer, filename);

        // todo, copy line number to dto, because we reverse it
        if (settings.isDescendingSortOfRows()) {
            Collections.reverse(lines);
        }
        for (int i = 0; i < lines.size(); i++) {
            applySettings(result, i);
        }

        return result;
    }

    private static void applySettings(final MoneyTransactionImportResultDto result, final int lineNumber) {
        final MoneyTransactionImportSettings settings = result.getSettings();
        final MoneyTransactionImportLine line = result.getLines().get(lineNumber);

        applyAmount(settings, line);
        applyTimestamp(settings, line);
        applyAccountNumber(settings, line);
        applyAccountName(settings, line);
        applyFinalBalance(settings, line);
        applyDescription(settings, line);
        applyCurrency(settings, line);
    }

    private static void applyCurrency(final MoneyTransactionImportSettings settings, final MoneyTransactionImportLine line) {
        if (settings.getCurrencyColumnIndex() != -1) {
            line.setCurrency(line.getCells().get(settings.getCurrencyColumnIndex()));
        }
        if (settings.getExchangeRateColumnIndex() != -1) {
            line.setExchangeRate(Util.getDoubleOrDefault(line.getCells().get(settings.getExchangeRateColumnIndex())));
        }
    }

    private static void applyDescription(final MoneyTransactionImportSettings settings, final MoneyTransactionImportLine line) {
        if (settings.getDescriptionColumnIndex() != -1) {
            line.setDescription(line.getCells().get(settings.getDescriptionColumnIndex()));
        }
    }

    private static void applyFinalBalance(final MoneyTransactionImportSettings settings, final MoneyTransactionImportLine line) {
        if (settings.getFinalBalanceColumnIndex() != -1) {
            line.setFinalBalance(Util.getDoubleOrDefault(line.getCells().get(settings.getFinalBalanceColumnIndex())));
        }
    }

    private static void applyAccountName(final MoneyTransactionImportSettings settings, final MoneyTransactionImportLine line) {
        if (settings.getCounterpartAccountNameColumnIndex() != -1) {
            line.setCounterpartAccountName(line.getCells().get(settings.getCounterpartAccountNameColumnIndex()));
        }
    }

    private static void applyAccountNumber(final MoneyTransactionImportSettings settings, final MoneyTransactionImportLine line) {
        if (settings.getCounterpartAccountNumberColumnIndex() != -1) {
            line.setCounterpartAccountNumber(line.getCells().get(settings.getCounterpartAccountNumberColumnIndex()));
        }
    }

    private static void applyTimestamp(final MoneyTransactionImportSettings settings, final MoneyTransactionImportLine line) {
        if (settings.getTimestampColumnIndex() != -1 && StringUtils.hasText(settings.getTimestampFormat())) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(settings.getTimestampFormat());
            final LocalDateTime dateTime = getLocalDateTime(settings, line, formatter);
            if (dateTime != null) {
                line.setTimestamp(dateTime.toInstant(ZoneOffset.UTC));
            }
        }
    }

    private static LocalDateTime getLocalDateTime(final MoneyTransactionImportSettings settings, final MoneyTransactionImportLine line, final DateTimeFormatter formatter) {
        LocalDateTime dateTime = null;
        try {
            dateTime = LocalDateTime.parse(line.getCells().get(settings.getTimestampColumnIndex()), formatter);
        } catch (final DateTimeParseException e) {
            try {
                dateTime = LocalDate.parse(line.getCells().get(settings.getTimestampColumnIndex()), formatter).atStartOfDay();
            } catch (final DateTimeParseException ee) {
                // do nothing
            }
        }
        return dateTime;
    }

    private static void applyAmount(final MoneyTransactionImportSettings settings, final MoneyTransactionImportLine line) {
        if (settings.getAmountColumnIndex() != -1) {
            double amount = Util.getDoubleOrDefault(line.getCells().get(settings.getAmountColumnIndex()));
            if (amount < 0) {
                line.setDebitCredit(DebitCredit.DEBIT);
            } else {
                final String creditIndication = settings.getDebitCreditColumnCreditIndication();
                final int creditColumnIndex = settings.getDebitCreditColumnIndex();

                if (creditColumnIndex != -1 && creditIndication != null) {
                    // if no match, for money it will probably be more debit than credit
                    line.setDebitCredit(creditIndication.equalsIgnoreCase(line.getCells().get(creditColumnIndex)) ? DebitCredit.CREDIT : DebitCredit.DEBIT);
                    if (line.getDebitCredit() == DebitCredit.DEBIT) {
                        amount = -amount;
                    }
                }
            }
            line.setAmount(amount);
        }
    }
}
