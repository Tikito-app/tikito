package org.tikito.service.MT940.lines;

import org.tikito.service.MT940.MT940Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MT940Line86AccountInfo extends MT940Line {
    private final Map<String, String> map = new HashMap<>();

    public MT940Line86AccountInfo(final String line) {
        super(line);
        parseAbnPart();
    }

    void parseAbnPart() {
        final List<String> parts = new ArrayList<>(List.of(line.split(":")));
        if (parts.size() > 1) {
            for (int i = 1; i < parts.size(); i++) {
                final String keyPart = parts.get(i - 1);
                final String valuePart = parts.get(i);
                String key = null;
                String value = null;

                for (int j = keyPart.length() - 1; j >= 0; j--) {
                    if (!Character.isLetter(keyPart.charAt(j))) {
                        key = keyPart.substring(j + 1).trim();
                        j = -1;
                    }
                }

                for (int j = valuePart.length() - 1; j >= 0; j--) {
                    if (!Character.isLetter(valuePart.charAt(j))) {
                        value = valuePart.substring(0, j).trim();
                        j = -1;
                    }
                }

                map.put(key, value);
            }
        }
    }

    public String getValue(final String key) {
        return map.get(key);
    }
}
