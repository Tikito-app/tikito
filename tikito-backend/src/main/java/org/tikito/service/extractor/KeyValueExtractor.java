package org.tikito.service.extractor;

import java.util.HashMap;
import java.util.Map;

public final class KeyValueExtractor {
    public static Map<String, String> extract(final String text) {
        final String[] split = text.split(":");
        final Map<String, String> map = new HashMap<>();
        for (int i = 1; i < split.length; i++) {
            final String previous = split[i - 1].trim();
            final String current = split[i].trim();
            final int startSpace = previous.lastIndexOf(" ");
            final int startR = previous.lastIndexOf("\r");
            final String key = previous.substring(Math.max(startSpace == -1 ? 0 : startSpace, startR == -1 ? 0 : startR)).trim();
            final int endSpace = current.lastIndexOf(" ");
            final int endR = current.lastIndexOf("\r");
            final int end = Math.max(endSpace == -1 ? 0 : endSpace, endR == -1 ? 0 : endR);
            final String value = current.substring(0, end == 0 ? current.length() : end).trim();
            map.put(key, value);
        }
        return map;
    }
}
