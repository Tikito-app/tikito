package org.tikito.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.UserPreferenceKey;
import org.tikito.entity.UserPreference;
import org.tikito.repository.UserPreferenceRepository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserPreferenceService {
    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreferenceService(final UserPreferenceRepository userPreferenceRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
    }

    public Map<UserPreferenceKey, Object> getAllUserPreferences(final long userId) {
        return userPreferenceRepository
                .findByUserId(userId)
                .stream()
                .filter(p -> p.mapValue() != null)
                .collect(Collectors.toMap(UserPreference::getValueKey, UserPreference::mapValue));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void setUserPreference(final long userId, final UserPreferenceKey key, final String value) {
        if (key == null) {
            return;
        }
        final UserPreference preference = userPreferenceRepository.findByUserIdAndValueKey(userId, key).orElse(new UserPreference(userId, key));
        preference.setValue(value);
        userPreferenceRepository.saveAndFlush(preference);
    }

    public <T> T get(final long userId, final UserPreferenceKey key, final T defaultValue) {
        final Optional<Object> maybeValue = userPreferenceRepository
                .findByUserIdAndValueKey(userId, key)
                .map(UserPreference::mapValue);
        return maybeValue.map(o -> (T) o).orElse(defaultValue);
    }
}
