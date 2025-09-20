package org.tikito.builder;

import org.tikito.dto.security.SecurityTransactionType;
import org.tikito.entity.security.SecurityTransaction;

public class SecurityTransactionBuilder {
    private final SecurityTransaction securityTransaction = new SecurityTransaction();

    public SecurityTransactionBuilder amount(final int amount) {
        securityTransaction.setAmount(amount);
        return this;
    }

    public SecurityTransactionBuilder price(final double price) {
        securityTransaction.setPrice(price);
        return this;
    }

    public SecurityTransactionBuilder type(final SecurityTransactionType transactionType) {
        securityTransaction.setTransactionType(transactionType);
        return this;
    }

    public SecurityTransaction build() {
        return securityTransaction;
    }
}
