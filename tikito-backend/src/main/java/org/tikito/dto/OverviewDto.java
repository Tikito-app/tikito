package org.tikito.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OverviewDto {
    private double positionValue = 0;
    private double totalDividend = 0;
    private double totalAdministrativeCosts = 0;
    private double totalTaxes = 0;
    private double totalTransactionCosts = 0;
    private double totalCashInvested = 0;
    private double totalCashWithdrawn = 0;
    private double worth = 0;
    private double maxCashInvested = 0;
    private double cashOnHand = 0;
}
