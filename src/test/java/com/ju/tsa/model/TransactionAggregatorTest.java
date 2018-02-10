package com.ju.tsa.model;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class TransactionAggregatorTest {

    @Test
    public void addTransactionToAWindow() {
        TransactionAggregator transactionAggregator = new TransactionAggregator(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, 1);
        TransactionAggregator updatedTransactionAggregator = transactionAggregator.append(new Transaction(BigDecimal.valueOf(3), 123L));

        Assert.assertEquals("overall sum should be the sum of all amounts", BigDecimal.valueOf(13), updatedTransactionAggregator.getSum());
        Assert.assertEquals("show the correct count of trx.", 2, updatedTransactionAggregator.getCount());
        Assert.assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(10), updatedTransactionAggregator.getMaximum());
        Assert.assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(3), updatedTransactionAggregator.getMinimum());
        Assert.assertEquals("average should be correct", new BigDecimal("6.50"), updatedTransactionAggregator.getAverage());
    }

    @Test
    public void flattenWindows() {

        TransactionAggregator transactionAggregatorOne = new TransactionAggregator(BigDecimal.valueOf(120), BigDecimal.valueOf(20), BigDecimal.valueOf(2), BigDecimal.valueOf(6), 20);
        TransactionAggregator transactionAggregatorTwo = new TransactionAggregator(BigDecimal.valueOf(60), BigDecimal.valueOf(20), BigDecimal.valueOf(2), BigDecimal.valueOf(12), 5);
        TransactionAggregator transactionAggregatorThree = new TransactionAggregator(BigDecimal.valueOf(250), BigDecimal.valueOf(250), BigDecimal.valueOf(250), BigDecimal.valueOf(250), 1);
        TransactionAggregator transactionAggregatorFour = new TransactionAggregator(BigDecimal.valueOf(900), BigDecimal.valueOf(300), BigDecimal.valueOf(20), BigDecimal.valueOf(150), 8);

        List<TransactionAggregator> transactionAggregators = Arrays.asList(transactionAggregatorOne, transactionAggregatorTwo, transactionAggregatorThree, transactionAggregatorFour);
        TransactionAggregator flattened = TransactionAggregator.flatten(transactionAggregators);

        Assert.assertEquals("overall sum should be the sum of all amounts", BigDecimal.valueOf(1330), flattened.getSum());
        Assert.assertEquals("show the correct count of trx.", 34, flattened.getCount());
        Assert.assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(300), flattened.getMaximum());
        Assert.assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(2), flattened.getMinimum());
        Assert.assertEquals("average should be correct", BigDecimal.valueOf(39.12), flattened.getAverage());
    }
}
