package com.ju.tsa.model;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * The aggregator of transactions. The goal was that the aggregator receives transactions one by one and updates
 * the state accordingly. Since it's important that multithreaded requests should be served, this was my attempt of
 * achieveing immutability by always returning a new object, rather than allowing who-knows-who mutating the data
 */
public class TransactionAggregator {

    private static final int ROUNDING_SCALE = 2;
    private final BigDecimal sum;
    private final BigDecimal max;
    private final BigDecimal min;
    private final BigDecimal average;
    private final long count;

    /**
     * The initial state of an aggregator is that there's no sum, no transactions aggregated, and no average is
     * calculated. The `max` and `min` are represented by MIN & MAX values because i want to update them correctly by
     * surely comparing values.
     */
    public TransactionAggregator() {
        this(BigDecimal.ZERO,
                BigDecimal.valueOf(Long.MIN_VALUE),
                BigDecimal.valueOf(Long.MAX_VALUE),
                BigDecimal.ZERO,
                BigDecimal.ZERO.longValue());
    }

    /**
     * Another constructor which will be used to compose a new aggregated state.
     *
     * @param sum     the new sum
     * @param max     the new maximum amount
     * @param min     the new minimum amount
     * @param average the new average
     * @param count   new count of transactions aggregated
     */
    TransactionAggregator(BigDecimal sum, BigDecimal max, BigDecimal min, BigDecimal average, long count) {
        this.sum = sum;
        this.max = max;
        this.min = min;
        this.average = average;
        this.count = count;
    }


    /**
     * Composes a new aggregator from the current data and the incoming Transaction object
     *
     * @param transaction the Transaction to be added
     * @return the TransactionAggregator with data from this TransactionAggregator and the next transaction aggregated
     */
    public TransactionAggregator append(Transaction transaction) {
        long newCount = count + 1;
        BigDecimal newMax = this.max.max(transaction.getAmount());
        BigDecimal newMin = this.min.min(transaction.getAmount());
        BigDecimal newSum = this.sum.add(transaction.getAmount());
        BigDecimal newAverage = newSum.divide(BigDecimal.valueOf(newCount), ROUNDING_SCALE, RoundingMode.HALF_EVEN);

        return new TransactionAggregator(newSum, newMax, newMin, newAverage, newCount);
    }

    //Note to myself, i like scala just for the sake of having a foldLeft in situations like these...
    public static TransactionAggregator flatten(List<TransactionAggregator> transactionAggregators) {
        long accumulatedCount = 0;
        BigDecimal overallMaximum = BigDecimal.valueOf(Long.MIN_VALUE);
        BigDecimal overallMinimum = BigDecimal.valueOf(Long.MAX_VALUE);
        BigDecimal accumulatedSum = BigDecimal.ZERO;

        for (TransactionAggregator transactionAggregator : transactionAggregators) {
            if (transactionAggregator == null) {
                continue;
            }

            accumulatedCount += transactionAggregator.getCount();
            overallMaximum = overallMaximum.max(transactionAggregator.getMaximum());
            overallMinimum = overallMinimum.min(transactionAggregator.getMinimum());
            accumulatedSum = accumulatedSum.add(transactionAggregator.getSum());
        }

        return newAggregator(accumulatedCount, overallMaximum, overallMinimum, accumulatedSum);
    }

    private static TransactionAggregator newAggregator(long accumulatedCount, BigDecimal overallMaximum, BigDecimal overallMinimum, BigDecimal accumulatedSum) {
        if (accumulatedCount != 0) {
            BigDecimal average = accumulatedSum.divide(BigDecimal.valueOf(accumulatedCount), ROUNDING_SCALE, BigDecimal.ROUND_HALF_EVEN);
            return new TransactionAggregator(
                    accumulatedSum,
                    overallMaximum,
                    overallMinimum,
                    average,
                    accumulatedCount);
        } else {
            return new TransactionAggregator();
        }
    }

    /**
     * Gets the average
     *
     * @return the average
     */
    public BigDecimal getAverage() {
        return average;
    }

    /**
     * Gets the sum
     *
     * @return sum
     */
    public BigDecimal getSum() {
        return sum;
    }

    /**
     * Gets the max
     *
     * @return max
     */
    public BigDecimal getMaximum() {
        return max;
    }

    /**
     * Gets the min
     *
     * @return min
     */
    public BigDecimal getMinimum() {
        return min;
    }

    /**
     * Gets the count
     *
     * @return the count
     */
    public long getCount() {
        return count;
    }
}
