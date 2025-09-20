package org.tikito.service.importer.security;

import org.tikito.dto.security.SecurityTransactionImportLine;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeGiroTransactionsImporter extends SecurityTransactionImporter {
    private static final List<String> DEGIRO_TRANSACTION_HEADERS = List.of(
            "Datum",
            "Tijd",
            "Product",
            "ISIN",
            "Beurs",
            "Uitvoeringsplaats",
            "Aantal",
            "Koers",
            "",
            "Lokale waarde",
            "",
            "Waarde",
            "",
            "Wisselkoers",
            "Transactiekosten en/of",
            "",
            "Totaal",
            "",
            "Order ID");

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    protected List<String> getHeaders() {
        return DEGIRO_TRANSACTION_HEADERS;
    }

    @Override
    public List<SecurityTransactionImportLine> map(final SecurityTransactionImportLine line) {
        final List<SecurityTransactionImportLine> lines = new ArrayList<>();
        lines.add(line);

        try {
            final LocalDate date = LocalDate.parse(line.getCells().get(0), dateFormatter);
            final LocalTime time = LocalTime.parse(line.getCells().get(1));

            line.setTimestamp(LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC));
            line.setProductName(line.getCells().get(2));
            line.setIsin(line.getCells().get(3));
            line.setAmount(Integer.parseInt(line.getCells().get(6)));
            line.setPrice(Double.parseDouble(line.getCells().get(7)));
            line.setCurrency(line.getCells().get(8));
//                    .transactionType(SecurityTransactionType.BUY_SELL) // todo
            // todo: create new line for extra costs
        } catch (final NumberFormatException | DateTimeParseException e) {
            line.setFailedReason(e.getMessage());
        }

        return lines;
    }
}
