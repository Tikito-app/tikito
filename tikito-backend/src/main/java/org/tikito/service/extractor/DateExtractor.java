package org.tikito.service.extractor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateExtractor {
    private static final String DATE_TIME_REGEX_DASHE_REGEX = "(.*) ([0-9]+)-([0-9]+)-([0-9]+) ([0-9]+):([0-9]+)(.*)";
    private static final String DATE_TIME_REGEX_SLASHED_REGEX = "(.*) ([0-9]+)\\.([0-9]+)\\.([0-9]+)/([0-9]+):([0-9]+)(.*)";

    public static Optional<Instant> extractDateDashed(final String text) {
        final Pattern pattern = Pattern.compile(DATE_TIME_REGEX_DASHE_REGEX);
        final Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(4));
            if (year < 100) {
                year += 2000;
            }
            final int month = Integer.parseInt(matcher.group(3));
            final int day = Integer.parseInt(matcher.group(2));
            final int hour = Integer.parseInt(matcher.group(5));
            final int minute = Integer.parseInt(matcher.group(6));

            return Optional.of(LocalDateTime.of(year, month, day, hour, minute).toInstant(ZoneOffset.UTC));
        }
        return Optional.empty();
    }

    public static Optional<Instant> extractDateSlashed(final String line) {
        final Pattern pattern = Pattern.compile(DATE_TIME_REGEX_SLASHED_REGEX);
        final Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(4));
            if (year < 100) {
                year += 2000;
            }
            final int month = Integer.parseInt(matcher.group(3));
            final int day = Integer.parseInt(matcher.group(2));
            final int hour = Integer.parseInt(matcher.group(5));
            final int minute = Integer.parseInt(matcher.group(6));

            return Optional.of(LocalDateTime.of(year, month, day, hour, minute).toInstant(ZoneOffset.UTC));
        }
        return Optional.empty();
    }
}
