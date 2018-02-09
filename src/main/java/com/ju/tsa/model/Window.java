package com.ju.tsa.model;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * TODO JAVADOC
 */
public class Window {

    public static final int ROUNDING_SCALE = 2;
    private final BigDecimal sum;
    private final BigDecimal max;
    private final BigDecimal min;
    private final BigDecimal average;
    private final long count;

    /**
     *
     */
    public Window() {
        this(BigDecimal.ZERO,
                BigDecimal.valueOf(Long.MIN_VALUE),
                BigDecimal.valueOf(Long.MAX_VALUE),
                BigDecimal.ZERO,
                BigDecimal.ZERO.longValue());
    }

    /**
     * @param sum
     * @param max
     * @param min
     * @param average
     * @param count
     */
    public Window(BigDecimal sum, BigDecimal max, BigDecimal min, BigDecimal average, long count) {
        this.sum = sum;
        this.max = max;
        this.min = min;
        this.average = average;
        this.count = count;
    }


    /**
     * @param transaction
     * @return
     */
    public Window withTransaction(Transaction transaction) {
        long newCount = count + 1;
        BigDecimal newMax = this.max.max(transaction.getAmount());
        BigDecimal newMin = this.min.min(transaction.getAmount());
        BigDecimal newSum = this.sum.add(transaction.getAmount());
        BigDecimal newAverage = newSum.divide(BigDecimal.valueOf(newCount), ROUNDING_SCALE, RoundingMode.HALF_EVEN);

        return new Window(newSum, newMax, newMin, newAverage, newCount);
    }

    //Note to myself, i like scala just for the sake of having a foldLeft in situations like these...
    public static Window flatten(List<Window> windows) {
        long accumulatedCount = 0;
        BigDecimal overallMaximum = BigDecimal.valueOf(Long.MIN_VALUE);
        BigDecimal overallMinimum = BigDecimal.valueOf(Long.MAX_VALUE);
        BigDecimal accumulatedSum = BigDecimal.ZERO;

        for (Window window : windows) {
            if (window == null) {
                continue;
            }

            accumulatedCount += window.getCount();
            overallMaximum = overallMaximum.max(window.getMaximum());
            overallMinimum = overallMinimum.min(window.getMinimum());
            accumulatedSum = accumulatedSum.add(window.getSum());
        }

        if (accumulatedCount != 0) {
            BigDecimal average = accumulatedSum.divide(BigDecimal.valueOf(accumulatedCount), ROUNDING_SCALE, BigDecimal.ROUND_HALF_EVEN);
            return new Window(
                    accumulatedSum,
                    overallMaximum,
                    overallMinimum,
                    average,
                    accumulatedCount);
        } else {
            return new Window();
        }
    }

    /**
     * @return
     */
    public BigDecimal getAverage() {
        return average;
    }

    /**
     * @return
     */
    public BigDecimal getSum() {
        return sum;
    }

    /**
     * @return
     */
    public BigDecimal getMaximum() {
        return max;
    }

    /**
     * @return
     */
    public BigDecimal getMinimum() {
        return min;
    }

    /**
     * @return
     */
    public long getCount() {
        return count;
    }
}
