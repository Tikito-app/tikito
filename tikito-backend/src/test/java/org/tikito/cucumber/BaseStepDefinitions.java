package org.tikito.cucumber;

import lombok.extern.slf4j.Slf4j;
import org.tikito.dto.export.ImportExportSettings;
import org.tikito.dto.security.HistoricalSecurityHoldingValueDto;
import org.tikito.dto.security.SecurityDto;
import org.tikito.repository.AccountRepository;
import org.tikito.service.BaseIntegrationTest;
import org.tikito.service.CacheService;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class BaseStepDefinitions extends BaseIntegrationTest {

    public static Long getNativeCurrencyId(final Map<String, String> map) {
        final Long currencyId = getCurrencyId(map);
        return currencyId == null ? 0 : currencyId;
    }

    public static Long getAccountId(final Map<String, String> map, final AccountRepository accountRepository) {
        final String account = map.get("account");
        if ("null".equals(account)) {
            return null;
        }
        return accountRepository.findByUserIdAndName(Long.parseLong(map.get("userId")), Set.of(account)).getFirst().getId();
    }

    public static Long getCurrencyId(final Map<String, String> map) {
        return getSecurityId(map, "currency");
    }

    public static Long getSecurityId(final Map<String, String> map) {
        return CacheService.getSecurityByName(map.get("security")).map(SecurityDto::getId).orElse(null);
    }

    public static Long getSecurityId(final String name) {
        return CacheService
                .getSecurityByName(name)
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

    protected <T> void equals(final List<Map<String, String>> expected, final List<T> persisted, final BiFunction<Map<String, String>, T, String> callback) {
        equals(expected, persisted, callback, true, null, null);
    }

    protected <T> void equals(final List<Map<String, String>> expected, final List<T> persisted, final BiFunction<Map<String, String>, T, String> callback, final boolean assertSizeEquals, final String dateKey, final Function<T, LocalDate> dateFunction) {
        if (assertSizeEquals && expected.size() != persisted.size()) {
            fail("Expected: \n" + expected + "\n\nBut found\n" + objectsToString(persisted));
        }

        for (final Map<String, String> expectedMap : expected) {
            boolean foundMatch = false;

            if (dateKey != null && dateFunction != null) {
                final List<T> entitiesOnDate = persisted.stream()
                        .filter(persistedEntity -> LocalDate.parse(expectedMap.get(dateKey)).equals(dateFunction.apply(persistedEntity)))
                        .toList();
                String result;
                final Set<String> propertiesMismatched = new HashSet<>();
                for (final T entity : entitiesOnDate) {
                    result = callback.apply(expectedMap, entity);
                    if(result == null) {
                        foundMatch = true;
                    } else {
                        propertiesMismatched.add(result);
                    }
                }

                if(!foundMatch) {
                    fail("No matching entity found for " + propertiesMismatched + ", expected: \n" + expectedMap + "\nGot\n" + objectsToString(entitiesOnDate));
                }
            } else {
                for (final T persistedEntity : persisted) {
                    final String result = callback.apply(expectedMap, persistedEntity);
                    if (result == null) {
                        foundMatch = true;
                        break;
                    }
                }
            }
            if (!foundMatch) {
                fail("No matching entity found for expected: \n" + expectedMap + "\nGot\n" + objectsToString(persisted));
            }
        }
    }

    private <T> String objectToString(final T obj) {
        final StringBuilder stringBuilder = new StringBuilder();
        objectToString(obj, stringBuilder);
        return stringBuilder.toString();
    }

    private <T> void objectToString(final T obj, final StringBuilder stringBuilder) {
        final Class<?> clazz = obj.getClass();
        final Field[] fields = clazz.getDeclaredFields();

        stringBuilder.append("{");

        for (int i = 0; i < fields.length; i++) {
            final Field field = fields[i];
            field.setAccessible(true);

            try {
                stringBuilder.append(field.getName())
                        .append("=")
                        .append(field.get(obj));
            } catch (final IllegalAccessException e) {
                stringBuilder.append(field.getName()).append("=<access denied>");
            }

            if (i < fields.length - 1) {
                stringBuilder.append(", ");
            }
        }

        if (obj instanceof final HistoricalSecurityHoldingValueDto dto) {
            stringBuilder.append(", performance=").append(SecurityTestHelper.getPerformance(dto));
        }

        stringBuilder.append("}\n");
    }

    private String objectsToString(final List<?> list) {
        final StringBuilder stringBuilder = new StringBuilder();
        list.forEach(obj -> {
            objectToString(obj, stringBuilder);
        });

        return stringBuilder.toString();
    }

    protected ImportExportSettings generateImportSettings() {
        final ImportExportSettings importExportSettings = new ImportExportSettings();
        importExportSettings.setAccounts(false);
        importExportSettings.setSecurityTransactions(false);
        importExportSettings.setLoans(false);
        importExportSettings.setMoneyTransactions(false);
        importExportSettings.setMoneyTransactionGroups(false);
        return importExportSettings;
    }
}
