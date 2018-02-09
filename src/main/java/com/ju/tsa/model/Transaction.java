package com.ju.tsa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 */
public class Transaction {
    private final BigDecimal amount;
    private final Instant transactionTimestamp;

    @JsonCreator
    public Transaction(@JsonProperty("amount") BigDecimal amount, @JsonProperty("timestamp") long timestamp) {
        this.amount = amount;
        this.transactionTimestamp = Instant.ofEpochMilli(timestamp);
    }

    /**
     * @return
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @return
     */
    public Instant getTransactionTimestamp() {
        return transactionTimestamp;
    }

}
