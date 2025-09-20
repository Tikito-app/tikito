package org.tikito.entity;

import org.tikito.dto.AccountDto;
import org.tikito.dto.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;

    private String name;
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private long currencyId;

    public Account(final long userId) {
        this.userId = userId;
    }

    public AccountDto toDto() {
        return new AccountDto(
                id,
                userId,
                name,
                accountType,
                accountNumber,
                currencyId);
    }
}
