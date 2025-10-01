package org.tikito.service.security;

import org.tikito.dto.security.IsinDto;
import org.tikito.entity.security.Isin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class IsinHelper {
    private IsinHelper() {
        // util class
    }

    public static Optional<Isin> getIsin(final List<Isin> isins, final LocalDate start, final LocalDate end) {
        if (isins == null || isins.isEmpty()) {
            return Optional.empty();
        }

        for (final Isin isin : isins) {
            if (isValid(isin, start, end)) {
                return Optional.of(isin);
            }
        }
        return Optional.empty();
    }


    public static boolean isValid(final Isin isin, final LocalDate start, final LocalDate end) {
        return isValid(isin.toDto(), start, end);
    }
    public static boolean isValid(final IsinDto isin, final LocalDate start, final LocalDate end) {
        if (isin.getValidFrom() == null) {
            if(start != null && isin.getValidTo() != null) {
                return start.isBefore(isin.getValidTo());
            }
            return true;
        } else if (start == null) {
            return false;
        }
        if (isin.getValidFrom().isAfter(start)) {
            return false;
        }

        if (isin.getValidTo() == null || (end == null && !start.isAfter(isin.getValidTo()))) {
            return true;
        } else if (end == null) {
            return false;
        }

        return isin.getValidTo().isAfter(end);
    }
}
