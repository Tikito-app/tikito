package org.tikito.entity;

import org.tikito.auth.AuthUser;
import org.tikito.auth.Scope;
import org.tikito.dto.UserAccountDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private boolean activated;
    private String activationCode;

    public UserAccount(final String email, final String password) {
        this.email = email;
        this.password = password;
        this.activationCode = UUID.randomUUID().toString();
    }

    public UserAccountDto toDto() {
        return new UserAccountDto(
                id,
                email);
    }

    public AuthUser toAuthUser() {
        return new AuthUser(
                null,
                id,
                email,
                Arrays.stream(Scope.values()).collect(Collectors.toSet()));
    }
}
