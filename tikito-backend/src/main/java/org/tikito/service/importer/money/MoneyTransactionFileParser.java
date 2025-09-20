package org.tikito.service.importer.money;

import java.util.List;

public abstract class MoneyTransactionFileParser {

    abstract public List<String> getHeaders();

    public boolean matchesHeader(final List<String> header) {
        final List<String> headersToMatch = getHeaders();
        if (header == null || header.size() != headersToMatch.size()) {
            return false;
        }
        for (int i = 0; i < headersToMatch.size(); i++) {
            if (!headersToMatch.get(i).equals(header.get(i))) {
                return false;
            }
        }
        return true;
    }

    public abstract MoneyTransactionImportSettings getSettings();
}
