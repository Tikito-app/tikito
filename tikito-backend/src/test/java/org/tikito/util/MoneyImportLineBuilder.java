package org.tikito.util;

import org.tikito.TestUtil;
import lombok.Getter;

@Getter
public class MoneyImportLineBuilder {
    private String counterpartAccountNumber;
    private String counterpartAccountName;
    private String timestamp;
    private String debitCredit;
    private String currency;
    private String code;
    private double amount;
    private double finalBalance;
    private String transactionType;
    private String description;

    public MoneyImportLineBuilder withRandom() {
        counterpartAccountNumber = TestUtil.randomIBAN();
        counterpartAccountName = TestUtil.randomString(5, 10);
        timestamp = "2025" + TestUtil.randomInt(10, 12) + TestUtil.randomInt(10, 27);
        debitCredit = TestUtil.randomBool() ? "Debit" : "Credit";
        currency = "EUR";
        amount = TestUtil.randomFloat(1, 100);
        finalBalance = TestUtil.randomFloat(1, 100);
        transactionType = TestUtil.randomString(5, 10);
        description = TestUtil.randomString(5, 30);
        return this;
    }

    public MoneyImportLineBuilder setCounterpartAccountNumber(final String counterpartAccountNumber) {
        this.counterpartAccountNumber = counterpartAccountNumber;
        return this;
    }

    public MoneyImportLineBuilder setCounterpartAccountName(final String counterpartAccountName) {
        this.counterpartAccountName = counterpartAccountName;
        return this;
    }

    public MoneyImportLineBuilder setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MoneyImportLineBuilder setDebitCredit(final String debitCredit) {
        this.debitCredit = debitCredit;
        return this;
    }

    public MoneyImportLineBuilder setCurrency(final String currency) {
        this.currency = currency;
        return this;
    }

    public MoneyImportLineBuilder setCode(final String code) {
        this.code = code;
        return this;
    }

    public MoneyImportLineBuilder setAmount(final double amount) {
        this.amount = amount;
        return this;
    }

    public MoneyImportLineBuilder setFinalBalance(final double finalBalance) {
        this.finalBalance = finalBalance;
        return this;
    }

    public MoneyImportLineBuilder setTransactionType(final String transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public MoneyImportLineBuilder setDescription(final String description) {
        this.description = description;
        return this;
    }
}