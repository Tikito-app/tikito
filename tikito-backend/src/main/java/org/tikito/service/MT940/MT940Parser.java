package org.tikito.service.MT940;

import org.tikito.service.money.enricher.MT940Enricher;

import java.util.ArrayList;
import java.util.List;

public final class MT940Parser {
    public static List<MT940Transaction> parse(final String data) {
        final List<MT940Block> blocks = MT940Reader.read(data);
        final List<MT940Transaction> transactions = new ArrayList<>();

        blocks.forEach(block -> transactions.addAll(block.getTransactions()));
        MT940Enricher.enrich(transactions);
        return transactions;
    }
}
