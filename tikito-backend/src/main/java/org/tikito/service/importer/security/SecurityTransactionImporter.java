package org.tikito.service.importer.security;

import org.tikito.dto.security.SecurityTransactionImportLine;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

public abstract class SecurityTransactionImporter {

    abstract protected List<String> getHeaders();

    public boolean matchesHeader(final List<String> header) {
        final List<String> headersToMatch = getHeaders();
        if (header == null || header.size() != headersToMatch.size()) {
            return false;
        }
        for (int i = 0; i < headersToMatch.size(); i++) {
            if (!headersMatch(headersToMatch.get(i), header.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean headersMatch(final String h1, final String h2) {
        if (!StringUtils.hasText(h1) && !StringUtils.hasText(h2)) {
            return true;
        }
        return Objects.equals(h1, h2);
    }

    /**
     * Map the csv to the dto.
     */
    public abstract List<SecurityTransactionImportLine> map(SecurityTransactionImportLine line);
}
