package org.tikito.entity;

import org.tikito.dto.AccountDto;
import org.tikito.dto.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.export.AccountExportDto;


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

    public Account(final long userId, final AccountExportDto export) {
        this.userId = userId;
        this.name = export.getName();
        this.accountType = export.getAccountType();
        this.accountNumber = export.getAccountNumber();
        this.currencyId = export.getCurrencyId();
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

    public AccountExportDto toExportDto() {
        return null;
//        new AccountExportDto(
//                name,
//                accountType,
//                accountNumber,
//                currencyId);
    }
}
