package com.ju.tsa.model;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionTimestampWithoutMillisTest {

    @Test
    public void flattenTimestampToSeconds() {
        Long time = 1518186844879L;
        Long expectedTime = 1518186844000L;
        Instant timeAsInstant = Instant.ofEpochMilli(time);
        Transaction transaction = new Transaction(BigDecimal.TEN, timeAsInstant.toEpochMilli());
        Assert.assertEquals(Instant.ofEpochMilli(expectedTime), transaction.strippedMillis());
    }
}
