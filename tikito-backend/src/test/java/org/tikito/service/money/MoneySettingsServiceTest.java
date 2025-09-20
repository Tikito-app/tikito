package org.tikito.service.money;

import org.tikito.dto.DebitCredit;
import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.service.importer.money.MoneyTransactionImportSettings;
import org.tikito.util.MoneyImportLineBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MoneySettingsServiceTest {
    final MoneyTransactionImportSettings.MoneyTransactionImportSettingsBuilder settingsBuilder = MoneyTransactionImportSettings.builder()
            .amountColumnIndex(0)
            .counterpartAccountNameColumnIndex(1)
            .counterpartAccountNumberColumnIndex(2)
            .debitCreditColumnIndex(3)
            .debitCreditColumnCreditIndication("Credit")
            .timestampColumnIndex(4)
            .timestampFormat("yyyyMMdd")
            .currencyColumnIndex(5)
            .finalBalanceColumnIndex(6)
            .descriptionColumnIndex(7);
    final MoneyImportLineBuilder lineBuilder = new MoneyImportLineBuilder().withRandom();

    @Test
    void testApply() {
        final MoneyTransactionImportSettings settings = settingsBuilder.build();
        final MoneyTransactionImportLine line = applySettingsGetFirst(settings, toLine(lineBuilder));

        assertEquals(lineBuilder.getCounterpartAccountNumber(), line.getCounterpartAccountNumber());
        assertEquals(lineBuilder.getCounterpartAccountName(), line.getCounterpartAccountName());
        assertEquals(lineBuilder.getFinalBalance(), line.getFinalBalance());
        assertEquals(lineBuilder.getDescription(), line.getDescription());
    }

    @Test
    void testApplyTimestampWithTimeToDateFormat() {
        final MoneyTransactionImportSettings settings = settingsBuilder.build();
        final MoneyTransactionImportLine line = applySettingsGetFirst(settings, toLine(lineBuilder));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(settings.getTimestampFormat());

        final LocalDate expectedTimestamp = LocalDate.parse(line.getCells().get(settings.getTimestampColumnIndex()), formatter);
        assertEquals(expectedTimestamp.atStartOfDay().toInstant(ZoneOffset.UTC), line.getTimestamp());
    }

    @Test
    void testApplyTimestampInvalidFormat() {
        final MoneyTransactionImportLine line = applySettingsGetFirst(settingsBuilder.build(), toLine(lineBuilder
                .setTimestamp("invalid-format")));
        assertNull(line.getTimestamp());
    }

    @Test
    void testApplyAmountAlwaysDebit() {
        final MoneyTransactionImportLine line = applySettingsGetFirst(settingsBuilder.build(), toLine(lineBuilder
                .setDebitCredit("Credit")
                .setAmount(-5)));
        assertEquals(-5, line.getAmount());
        assertEquals(DebitCredit.DEBIT, line.getDebitCredit());
    }

    @Test
    void testApplyAmountDebitOtherCase() {
        final MoneyTransactionImportLine line = applySettingsGetFirst(settingsBuilder.build(), toLine(lineBuilder
                .setDebitCredit("DEBIT")
                .setAmount(5)));
        assertEquals(-5, line.getAmount());
        assertEquals(DebitCredit.DEBIT, line.getDebitCredit());
    }

    @Test
    void testApplyAmountCredit() {
        final MoneyTransactionImportLine line = applySettingsGetFirst(settingsBuilder.build(), toLine(lineBuilder
                .setDebitCredit("Credit")
                .setAmount(5)));
        assertEquals(5, line.getAmount());
        assertEquals(DebitCredit.CREDIT, line.getDebitCredit());
    }

    @Test
    void testApplyAmountCreditNull() {
        final MoneyTransactionImportLine line = applySettingsGetFirst(settingsBuilder.build(), toLine(lineBuilder
                .setDebitCredit(null)
                .setAmount(5)));
        assertEquals(-5, line.getAmount());
        assertEquals(DebitCredit.DEBIT, line.getDebitCredit());
    }

    @Test
    void testApplyAmountCreditNonNullNoMatch() {
        final MoneyTransactionImportLine line = applySettingsGetFirst(settingsBuilder.build(), toLine(lineBuilder
                .setDebitCredit("invalid-value")
                .setAmount(5)));
        assertEquals(-5, line.getAmount());
        assertEquals(DebitCredit.DEBIT, line.getDebitCredit());
    }

    private MoneyTransactionImportLine applySettingsGetFirst(final MoneyTransactionImportSettings build, final List<List<String>> line) {
        final MoneyTransactionImportResultDto result = MoneySettingsService.applySettings(build, line, null, "Filename");
        assertEquals(1, result.getLines().size());
        return result.getLines().getFirst();
    }

    private List<List<String>> toLine(final MoneyImportLineBuilder lineBuilder) {
        final List<String> list = new ArrayList<>();
        list.add(Double.toString(lineBuilder.getAmount()));
        list.add(lineBuilder.getCounterpartAccountName());
        list.add(lineBuilder.getCounterpartAccountNumber());
        list.add(lineBuilder.getDebitCredit());
        list.add(lineBuilder.getTimestamp());
        list.add(lineBuilder.getCurrency());
        list.add(Double.toString(lineBuilder.getFinalBalance()));
        list.add(lineBuilder.getDescription());
        return List.of(list);
    }
}