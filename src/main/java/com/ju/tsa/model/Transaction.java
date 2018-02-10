package com.ju.tsa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Models the incoming data of the transaction consisting of an amount and a timestamp of a transaction.
 * <p>
 * I'm going for the Instant to keep track of the timing here, as i'm foolishly hoping i'll benefit from it.
 */
public class Transaction {
    private final BigDecimal amount;
    private final Instant transactionTimestamp;
    private final Instant timestampStrippedFromMillis;

    @JsonCreator
    public Transaction(@JsonProperty("amount") BigDecimal amount, @JsonProperty("timestamp") long timestamp) {
        this.amount = amount;
        this.transactionTimestamp = Instant.ofEpochMilli(timestamp);
        this.timestampStrippedFromMillis = transactionTimestamp.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Fetches the amount of the transaction
     *
     * @return amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Fetches the timestamp of the transaction
     *
     * @return the timestamps with exact millis of a Transaction.
     */
    public Instant getTransactionTimestamp() {
        return transactionTimestamp;
    }

    /**
     * Returns the timestamp in the good old epoch timestamp size, i just stripped off the milliseconds from it.
     * This will be useful when i start using the second based timestamps as keys for the aggregates
     *
     * @return the timestamp of a transaction to a precision of a second
     */
    public Instant strippedMillis() {
        return timestampStrippedFromMillis;
    }
}
