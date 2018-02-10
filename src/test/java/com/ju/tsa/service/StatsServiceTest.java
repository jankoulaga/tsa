package com.ju.tsa.service;

import com.ju.tsa.model.Transaction;
import com.ju.tsa.model.TransactionAggregator;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class StatsServiceTest {

    @Test
    public void addTransaction() {
        Clock fakeTimeKeeper = Clock.systemDefaultZone();
        StatsService stats = new StatsService(fakeTimeKeeper);
        Instant now = Instant.now();
        stats.add(new Transaction(BigDecimal.valueOf(20), now.minusSeconds(1).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(3), now.minusSeconds(12).toEpochMilli()));

        TransactionAggregator result = stats.aggregate();

        assertEquals("overall sum should be the sum of two trx. amounts", BigDecimal.valueOf(23), result.getSum());
        assertEquals("there were only two transactions", 2, result.getCount());
        assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(20), result.getMaximum());
        assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(3), result.getMinimum());
        assertEquals("average should be correct", new BigDecimal("11.50"), result.getAverage());
    }

    @Test
    public void addTransactionAfterWindowScope() {
        Clock fakeTimeKeeper = Clock.systemUTC();
        Instant now = Instant.now();
        StatsService stats = new StatsService(fakeTimeKeeper);


        stats.add(new Transaction(BigDecimal.valueOf(5), now.plusSeconds(530).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(10), now.minusMillis(640).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(15), now.minusMillis(650).toEpochMilli()));

        TransactionAggregator result = stats.aggregate();

        assertEquals("overall sum should be the sum of all amounts", BigDecimal.valueOf(25), result.getSum());
        assertEquals("show the correct count of trx.", 2, result.getCount());
        assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(15), result.getMaximum());
        assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(10), result.getMinimum());
        assertEquals("average should be correct", new BigDecimal("12.50"), result.getAverage());
    }

    @Test
    public void addTransactionToMultipleWindowsWithoutAnySpecificOrder() {
        Clock fakeTimeKeeper = Clock.systemDefaultZone();
        StatsService stats = new StatsService(fakeTimeKeeper);
        Instant now = Instant.now();
        stats.add(new Transaction(BigDecimal.valueOf(10), now.minusSeconds(20).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(5), now.minusSeconds(10).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(15), now.minusSeconds(30).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(20), now.minusSeconds(50).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(-10), now.minusSeconds(40).toEpochMilli()));

        TransactionAggregator result = stats.aggregate();

        assertEquals("overall sum should be the sum of all amounts", BigDecimal.valueOf(40), result.getSum());
        assertEquals("show the correct count of trx.", 5, result.getCount());
        assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(20), result.getMaximum());
        assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(-10), result.getMinimum());
        assertEquals("average should be correct", new BigDecimal("8.00"), result.getAverage());
    }

    @Test
    public void addTransactionToSingleWindow() {
        Clock fakeTimeKeeper = Clock.systemDefaultZone();
        StatsService stats = new StatsService(fakeTimeKeeper);

        Instant now = Instant.now(fakeTimeKeeper);

        stats.add(new Transaction(BigDecimal.valueOf(5), now.minusMillis(3).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(10), now.minusMillis(7).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(15), now.minusMillis(9).toEpochMilli()));

        TransactionAggregator result = stats.aggregate();

        assertEquals("overall sum should be the sum of all amounts", BigDecimal.valueOf(30), result.getSum());
        assertEquals("show the correct count of trx.", 3, result.getCount());
        assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(15), result.getMaximum());
        assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(5), result.getMinimum());
        assertEquals("average should be correct", new BigDecimal("10.00"), result.getAverage());
    }

    @Test
    public void addTransactionBeforeWindowScope() {
        Clock fakeTimeKeeper = Clock.systemUTC();
        Instant now = Instant.now();
        StatsService stats = new StatsService(fakeTimeKeeper);


        stats.add(new Transaction(BigDecimal.valueOf(5), now.minusSeconds(530).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(10), now.minusMillis(640).toEpochMilli()));
        stats.add(new Transaction(BigDecimal.valueOf(15), now.minusMillis(650).toEpochMilli()));

        TransactionAggregator result = stats.aggregate();

        assertEquals("overall sum should be the sum of all amounts", BigDecimal.valueOf(25), result.getSum());
        assertEquals("show the correct count of trx.", 2, result.getCount());
        assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(15), result.getMaximum());
        assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(10), result.getMinimum());
        assertEquals("average should be correct", new BigDecimal("12.50"), result.getAverage());
    }

}
