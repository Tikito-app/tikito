package org.tikito.repository;

import org.tikito.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserIdAndValueKey(long userId, String key);

    List<UserPreference> findByUserId(long userId);
}
