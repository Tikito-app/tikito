package org.tikito.service.MT940;

import org.tikito.service.MT940.lines.MT940Line61Statement;
import org.tikito.service.MT940.lines.MT940Line86AccountInfo;

import java.util.ArrayList;
import java.util.List;

public class MT940Block {
    private final List<MT940Line> lines;

    public MT940Block(final List<MT940Line> lines) {
        this.lines = lines;
    }

    public List<MT940Transaction> getTransactions() {
        final List<MT940Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < lines.size() - 1; i++) {
            if (lines.get(i) instanceof final MT940Line61Statement line) {
                if (lines.get(i + 1) instanceof final MT940Line86AccountInfo nextLine) {
                    final MT940Transaction transaction = new MT940Transaction();
                    transaction.setDate(line.getValueDate());
                    transaction.setAmount(line.getAmount());
                    transaction.setToAccountName(nextLine.getValue("NAAM"));
                    transaction.setToAccountName(nextLine.getValue("IBAN"));
                    transaction.setDescription(nextLine.getLine());
                    transactions.add(transaction);
                    i++;
                } else {
                    throw new RuntimeException("Error");
                }
            }
        }
        return transactions;
    }
}
