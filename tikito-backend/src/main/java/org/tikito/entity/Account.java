package org.tikito.entity;

import org.tikito.dto.AccountDto;
import org.tikito.dto.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.export.AccountExportDto;
import org.tikito.entity.security.Security;

import java.util.Map;


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

    public Account(final long userId, final AccountExportDto export, final Map<String, Security> currenciesByIsin) {
        this.userId = userId;
        this.name = export.getName();
        this.accountType = export.getAccountType();
        this.accountNumber = export.getAccountNumber();
        this.currencyId = currenciesByIsin.get(export.getCurrency()).getId();
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

    public AccountExportDto toExportDto(final Map<Long, Security> currenciesById) {
        return new AccountExportDto(
                name,
                accountType,
                accountNumber,
                currenciesById.get(currencyId).getCurrentIsin());
    }
}
