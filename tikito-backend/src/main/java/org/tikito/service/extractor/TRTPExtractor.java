package org.tikito.service.extractor;

import java.util.HashMap;
import java.util.Map;

public final class TRTPExtractor {
    public static Map<String, String> extract(final String text) {
        final String[] split = text.split("/");
        final Map<String, String> map = new HashMap<>();
        for (int i = 2; i < split.length; i += 2) {
            map.put(split[i - 1].trim(), split[i].trim());
        }
        return map;
    }
}
