package org.tikito.cucumber;

import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.tikito.dto.security.SecurityDto;
import org.tikito.service.BaseIntegrationTest;
import org.tikito.service.CacheService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class BaseStepDefinitions extends BaseIntegrationTest {


    public static Long getNativeCurrencyId(final Map<String, String> map) {
        final Long currencyId = getCurrencyId(map);
        return currencyId == null ? 0 : currencyId;
    }

    public static Long getCurrencyId(final Map<String, String> map) {
        return getSecurityId(map, "currency");
    }

    public static Long getSecurityId(final Map<String, String> map) {
        return getSecurityId(map, "security");
    }

    public static Long getSecurityId(final String name) {
        return CacheService
                .getSecurityByName("security")
                .map(SecurityDto::getId)
                .orElse(null);
    }

    public static Long getSecurityId(final Map<String, String> map, final String identifier) {
        final String currency = map.get(identifier);
        if (currency == null) {
            return null;
        }
        return CacheService.getSecurityByIsin(currency)
                .map(SecurityDto::getId)
                .orElse(null);
    }

    public static long getNativeLong(final Map<String, String> entry, final String key) {
        if (entry.containsKey(key)) {
            try {
                return Long.parseLong(entry.get(key));
            } catch (final NumberFormatException _) {
                return 0;
            }
        }
        return 0;
    }

    public static int getNativeInt(final Map<String, String> map, final String key) {
        return (int) getNativeLong(map, key);
    }

    public static double getNativeDouble(final Map<String, String> map, final String key) {
        if (map.containsKey(key)) {
            try {
                return Double.parseDouble(map.get(key));
            } catch (final NumberFormatException _) {
                return 0;
            }
        }
        return 0;
    }

    public static Long getLong(final Map<String, String> entry, final String key) {
        if (entry.containsKey(key)) {
            try {
                return Long.parseLong(entry.get(key));
            } catch (final NumberFormatException _) {
                return null;
            }
        }
        return null;
    }

    protected <T> void equals(final List<Map<String, String>> expected, final List<T> persisted, final BiPredicate<Map<String, String>, T> callback) {
        if (expected.size() != persisted.size()) {
            fail("Expected: \n" + expected + "\n\nBut found\n" + objectsToString(persisted));
        }

        for (final Map<String, String> expectedMap : expected) {
            boolean foundMatch = false;
            for (final T persistedEntity : persisted) {
                if (callback.test(expectedMap, persistedEntity)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                fail("No matching entity found for expected: \n" + expectedMap + "\nGot\n" + objectsToString(persisted));
            }
        }
    }

    private String objectsToString(final List<?> list) {
        final StringBuilder stringBuilder = new StringBuilder();
        list.forEach(obj -> {
            final Class<?> clazz = obj.getClass();
            final Field[] fields = clazz.getDeclaredFields();

            stringBuilder.append(clazz.getSimpleName()).append("{");

            for (int i = 0; i < fields.length; i++) {
                final Field field = fields[i];
                field.setAccessible(true);

                try {
                    stringBuilder.append(field.getName())
                            .append("=")
                            .append(field.get(obj));
                } catch (IllegalAccessException e) {
                    stringBuilder.append(field.getName()).append("=<access denied>");
                }

                if (i < fields.length - 1) {
                    stringBuilder.append(", ");
                }
            }

            stringBuilder.append("}\n");
        });

        return stringBuilder.toString();
    }
}
