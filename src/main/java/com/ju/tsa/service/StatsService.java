package com.ju.tsa.service;

import com.ju.tsa.model.Transaction;
import com.ju.tsa.model.TransactionAggregator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;


@Scope("singleton")
@Service
public class StatsService {

    private static final int STATS_DURATION_MEASUREMENT_SECONDS = 60;
    private static final int STATS_DURATION_MEASUREMENT_MILLIS = STATS_DURATION_MEASUREMENT_SECONDS * 1000;

    /**
     * Array of aggregators. Each TransactionAggregator at a given position will handle
     * all the transactions for a seconds worth of transactions
     **/
    private TransactionAggregator[] openTransactionAggregators = new TransactionAggregator[STATS_DURATION_MEASUREMENT_SECONDS];

    /**
     * Array of second based instants which are used to keep track of the indexing for openTransactionAggregators.
     * This will be used to find the index matching the second portion of a transaction, to find the correct Aggregator
     * for that second
     **/
    private Instant[] windowTimestamps = new Instant[STATS_DURATION_MEASUREMENT_SECONDS];

    private final Clock timeKeeper;

    public StatsService() {
        this.timeKeeper = Clock.systemUTC();
    }

    public StatsService(Clock timeKeeper) {
        this.timeKeeper = timeKeeper;
    }

    /**
     * Add a transaction to the stats. I guess this should be synchronised as it's the entry point to the stats logic.
     *
     * @param transaction Trx to be accumulated within the window
     */
    public synchronized void add(Transaction transaction) {
        if (fitsCurrentTimeSpan(transaction)) {
            boolean windowCleaningNeeded = shouldTransactionCloseWindow(transaction);
            //Close the window and, create a new aggregator in it's place
            if (windowCleaningNeeded) {
                resetAggregator(transaction);
            }

            updateWindowTimestamp(transaction);
            setAggregatorForTransaction(transaction, getWindowForTransaction(transaction).append(transaction));
        }

    }

    /**
     * Return the accumulated aggregator. This means that a list of open windows and corresponding sub-aggregators
     * needs to be fetched
     *
     * @return a single TransactionAggregator which will represent the accumulated data from subsequent aggregators that
     * hold aggregated data
     */
    public TransactionAggregator aggregate() {
        return TransactionAggregator.flatten(getOpenTransactionAggregators());
    }

    /**
     * Returns the list of Aggregators that belong to open windows.
     *
     * @return TransactionAggregator belonging to windows that have been started and whose timestamps are no more than
     * 60 seconds old
     */
    private List<TransactionAggregator> getOpenTransactionAggregators() {
        /*
         *We're talking millis here. I want to cache time now, so all elements in a list have the sam information
         *on when the window started, just in case the calculation is slower due to something else slowing down the CPU
         *It might happen, that this is triggered at millis *999, and if i recalculate the windowStart inside, then the
         *seconds portion of the Instant(which is the index of the array) might be bumped up. That would make me very sad.
         */
        Instant windowStart = Instant.from(timeKeeper.instant()).minusMillis(STATS_DURATION_MEASUREMENT_MILLIS);

        // Since i'm iterating, let's make it a Linked list
        LinkedList<TransactionAggregator> openWindowsWithAggregators = new LinkedList<>();

        for (int i = 0; i < openTransactionAggregators.length; i++) {
            TransactionAggregator transactionAggregator = openTransactionAggregators[i];

            if (transactionAggregator != null && windowTimestamps[i].isAfter(windowStart)) {
                openWindowsWithAggregators.push(transactionAggregator);
            }
        }

        return openWindowsWithAggregators;
    }

    /**
     * Now... If the transaction timestamp is before the time the timeKepper says it is, but still in range
     * we're interested in (60 secs) or, the transaction is definitely not in a galaxy far far away where the
     * time space continuum is completely broken, we're interested in it's data.
     *
     * @param transaction the Transaction we're testing
     * @return true if the Transaction.timestamp is in our time span of interest, false otherwise
     */
    private boolean fitsCurrentTimeSpan(Transaction transaction) {
        Instant currentTimekeeperInstant = timeKeeper.instant();
        Instant trxInstant = transaction.getTransactionTimestamp();
        boolean isTransactionInFuture = trxInstant.isBefore(currentTimekeeperInstant);
        boolean isTransactionTooFarBehind = currentTimekeeperInstant.minusMillis(STATS_DURATION_MEASUREMENT_MILLIS).isAfter(trxInstant);
        return isTransactionInFuture || isTransactionTooFarBehind;
    }


    /**
     * If the given transaction is the one opening the window (null case) or the transaction coming is way to old for our
     * liking, this method will tell us that. Basically it's purpose is to notify it's caller that it's time to reset the
     * aggregator for the index in question
     *
     * @param transaction the inbound Transaction
     * @return true if current window for the transaction second portion is empty, or the window for the transaction
     * doesn't match the window the transaction should be in
     */
    private boolean shouldTransactionCloseWindow(Transaction transaction) {
        return getWindowForTransaction(transaction) == null
                || getSecondsKeyForTransaction(transaction) != transaction.strippedMillis().getEpochSecond();
    }

    /**
     * Returns the aggregator at a second index. For example, if the transaction occurred at 11:32:45, this will return
     * the Aggregator at position 45 of the aggregators list.
     *
     * @param transaction the inbound Transaction
     * @return the TransactionAggregator responsible for a given second of a transaction
     */
    private TransactionAggregator getWindowForTransaction(Transaction transaction) {
        return openTransactionAggregators[getWindowIndexForTransaction(transaction)];
    }

    /**
     * Returns the timestamp in seconds responsible for the inbound Transaction
     *
     * @param transaction the Transaction for which the second based timestamp needs to be evaluated
     * @return the timestamp up to seconds at an index for this transaction
     */
    private long getSecondsKeyForTransaction(Transaction transaction) {
        return windowTimestamps[getWindowIndexForTransaction(transaction)].getEpochSecond();
    }

    /**
     * This will give me the index in the array, based on seconds. E.g. if a trx occurred at 12:34:21, then the index
     * will be 21
     *
     * @param transaction the inbound Transaction
     * @return a number between 0 and 59 representing the window index which will handle the aggregation
     */
    private int getWindowIndexForTransaction(Transaction transaction) {
        return (int) (transaction.strippedMillis().getEpochSecond()) % STATS_DURATION_MEASUREMENT_SECONDS;
    }

    /**
     * Set the seconds timestamp at a given index from an inbound Transaction
     *
     * @param transaction the Transaction which is opening a new window
     */
    private void updateWindowTimestamp(Transaction transaction) {
        windowTimestamps[getWindowIndexForTransaction(transaction)] = transaction.strippedMillis();
    }

    /**
     * Set's an aggregator for a window to which the inbound Transaction belongs to
     *
     * @param transaction           inbound Transaction
     * @param transactionAggregator the TransactionAggregator to be responsible at evaluated index for a given Transaction
     */
    private void setAggregatorForTransaction(Transaction transaction, TransactionAggregator transactionAggregator) {
        openTransactionAggregators[getWindowIndexForTransaction(transaction)] = transactionAggregator;
    }

    /**
     * Sets a new TransactionAggregator for a Transaction
     *
     * @param transaction the inbound Transaction
     */
    private void resetAggregator(Transaction transaction) {
        setAggregatorForTransaction(transaction, new TransactionAggregator());
    }

}
