package org.tikito.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;
import org.tikito.dto.UserPreferenceKey;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    @Enumerated(EnumType.STRING)
    private UserPreferenceKey valueKey;
    private String value;

    public UserPreference(final long userId, final UserPreferenceKey valueKey) {
        this(userId, valueKey, null);
    }

    public UserPreference(final long userId, final UserPreferenceKey valueKey, final String value) {
        this.userId = userId;
        this.valueKey = valueKey;
        this.value = value;
    }

    public Object mapValue() {
        if (!StringUtils.hasText(value)) {
            return null;
        } else if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        } else {
            try {
                return Double.parseDouble(value);
            } catch (final NumberFormatException e) {
                try {
                    return Integer.parseInt(value);
                } catch (final NumberFormatException ee) {
                    return value;
                }
            }
        }
    }
}
