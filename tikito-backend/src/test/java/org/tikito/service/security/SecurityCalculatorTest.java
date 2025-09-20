package org.tikito.service.security;

import org.junit.jupiter.api.Test;
import org.tikito.builder.SecurityTransactionBuilder;
import org.tikito.dto.security.HistoricalSecurityHoldingValueDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tikito.dto.security.SecurityTransactionType.BUY;
import static org.tikito.dto.security.SecurityTransactionType.SELL;

public class SecurityCalculatorTest {

    @Test
    void shouldHaveProperCashValues_when_sellTransactionLessThanOriginalAmount() {
        final HistoricalSecurityHoldingValueDto value = HistoricalSecurityHoldingValueDto.builder().build();

        SecurityCalculator.applyTransaction(value, new SecurityTransactionBuilder().price(-2).amount(5).type(BUY).build());
        assertEquals(-10, value.getMaxCashInvested());
        assertEquals(0, value.getCashOnHand());

        SecurityCalculator.applyTransaction(value, new SecurityTransactionBuilder().price(1).amount(3).type(SELL).build());
        assertEquals(-10, value.getMaxCashInvested());
        assertEquals(3, value.getCashOnHand());
    }

    @Test
    void shouldHaveProperCashValues_when_sellTransactionMoreThanOriginalAmount() {
        final HistoricalSecurityHoldingValueDto value = HistoricalSecurityHoldingValueDto.builder().build();

        SecurityCalculator.applyTransaction(value, new SecurityTransactionBuilder().price(-2).amount(5).type(BUY).build());
        assertEquals(-10, value.getMaxCashInvested());
        assertEquals(0, value.getCashOnHand());

        SecurityCalculator.applyTransaction(value, new SecurityTransactionBuilder().price(4).amount(3).type(SELL).build());
        assertEquals(-10, value.getMaxCashInvested());
        assertEquals(12, value.getCashOnHand());
    }

    @Test
    void shouldHaveProperCashValues_when_buyTransactionWithCashOnHandAndLessAmountThanCashOnHand() {
        final HistoricalSecurityHoldingValueDto value = HistoricalSecurityHoldingValueDto.builder().amount(10)
                .cashOnHand(10)
                .maxCashInvested(-10).build();

        SecurityCalculator.applyTransaction(value, new SecurityTransactionBuilder().price(-2).amount(3).type(BUY).build());
        assertEquals(-10, value.getMaxCashInvested());
        assertEquals(4, value.getCashOnHand());
    }

    @Test
    void shouldHaveProperCashValues_when_buyTransactionWithCashOnHandAndMoreAmountThanCashOnHand() {
        final HistoricalSecurityHoldingValueDto value = HistoricalSecurityHoldingValueDto.builder().amount(10)
                .cashOnHand(10)
                .maxCashInvested(-10).build();

        SecurityCalculator.applyTransaction(value, new SecurityTransactionBuilder().price(-5).amount(3).type(BUY).build());
        assertEquals(-15, value.getMaxCashInvested());
        assertEquals(0, value.getCashOnHand());
    }
}
