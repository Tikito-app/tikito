package org.tikito.service.extractor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateExtractor {

    private static final String TIME_GROUP = "(?:[ T/](\\d{1,2}):(\\d{2})(?::(\\d{2}))?)?";

    private static final Pattern YEAR_FIRST_PATTERN = Pattern.compile(
            "(?<!\\d)(\\d{4})([-./])(\\d{1,2})\\2(\\d{1,2})" + TIME_GROUP + "(?!\\d)");

    private static final Pattern DAY_FIRST_PATTERN = Pattern.compile(
            "(?<!\\d)(\\d{1,2})([-./])(\\d{1,2})\\2(\\d{2,4})" + TIME_GROUP + "(?!\\d)");

    private DateExtractor() {
    }

    public static Optional<Instant> extractDate(final String text) {
        if (text == null) {
            return Optional.empty();
        }

        return findMatch(YEAR_FIRST_PATTERN, text, true)
                .or(() -> findMatch(DAY_FIRST_PATTERN, text, false));
    }

    private static Optional<Instant> findMatch(final Pattern pattern, final String text, final boolean yearFirst) {
        final Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            final Optional<Instant> instant = toInstant(matcher, yearFirst);
            if (instant.isPresent()) {
                return instant;
            }
        }
        return Optional.empty();
    }

    private static Optional<Instant> toInstant(final Matcher matcher, final boolean yearFirst) {
        try {
            int year;
            int month;
            int day;

            if (yearFirst) {
                year = Integer.parseInt(matcher.group(1));
                month = Integer.parseInt(matcher.group(3));
                day = Integer.parseInt(matcher.group(4));
            } else {
                day = Integer.parseInt(matcher.group(1));
                month = Integer.parseInt(matcher.group(3));
                year = Integer.parseInt(matcher.group(4));
                if (year < 100) {
                    year += 2000;
                }
                if (month > 12 && day <= 12) {
                    final int swap = month;
                    month = day;
                    day = swap;
                }
            }

            if (month < 1 || month > 12 || day < 1 || day > 31) {
                return Optional.empty();
            }

            final int hour = matcher.group(5) != null ? Integer.parseInt(matcher.group(5)) : 0;
            final int minute = matcher.group(6) != null ? Integer.parseInt(matcher.group(6)) : 0;
            final int second = matcher.group(7) != null ? Integer.parseInt(matcher.group(7)) : 0;

            return Optional.of(LocalDateTime.of(year, month, day, hour, minute, second).toInstant(ZoneOffset.UTC));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }
}
