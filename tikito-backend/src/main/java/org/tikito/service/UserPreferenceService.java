package org.tikito.service;

import org.tikito.entity.UserPreference;
import org.tikito.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserPreferenceService {
    private final UserPreferenceRepository userPreferenceRepository;
    private final Set<String> validKeys = new HashSet<>();

    public UserPreferenceService(final UserPreferenceRepository userPreferenceRepository) {
        this.userPreferenceRepository = userPreferenceRepository;

        validKeys.add("AGGREGATE_DATE_RANGE");
        validKeys.add("GROUP_IDS");
        validKeys.add("START_AT_ZERO_FROM_BEGINNING");
        validKeys.add("SECURITY_START_AT_ZERO_FROM_BEGINNING");
        validKeys.add("START_AT_ZERO_AFTER_DATE_RANGE");
        validKeys.add("SECURITY_START_AT_ZERO_AFTER_DATE_RANGE");
        validKeys.add("TOTAL_VALUE");
        validKeys.add("DATE_RANGE");
        validKeys.add("SECURITY_DATE_RANGE");
        validKeys.add("START_DATE");
        validKeys.add("END_DATE");
        validKeys.add("ACCOUNT_IDS");
        validKeys.add("MONEY_SHOW_OTHER");
        validKeys.add("AMOUNT_OF_OTHER_GROUPS");
        validKeys.add("SHOW_CLOSED_POSITIONS");
        validKeys.add("IMPORT_DATE_FORMAT");
    }

    public Map<String, Object> getAllUserPreferences(final long userId) {
        return userPreferenceRepository
                .findByUserId(userId)
                .stream()
                .filter(p -> p.mapValue() != null)
                .collect(Collectors.toMap(UserPreference::getValueKey, UserPreference::mapValue));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void setUserPreference(final long userId, final String key, final String value) {
        if (!validKeys.contains(key)) {
            return;
        }
        final UserPreference preference = userPreferenceRepository.findByUserIdAndValueKey(userId, key).orElse(new UserPreference(userId, key));
        preference.setValue(value);
        userPreferenceRepository.saveAndFlush(preference);
    }
}
