package org.tikito.dto.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class SecurityHoldingDto {
    private Long id;
    private long userId;
    private Set<Long> accountIds;
    private Long securityId;
    private long currencyId;
    private int amount;
    private double price;
    private double totalDividend;
    private double totalAdministrativeCosts;
    private double totalTaxes;
    private double totalTransactionCosts;
    private double totalCashInvested;
    private double totalCashWithdrawn;
    private double worth;
    private double maxCashInvested;
    private double cashOnHand;

    // for front end
    private SecurityDto security;
}
