# tsa

Receives a JSON representation of a Transaction and does nothing with the transaction itself, except it aggregates all the amounts it receives
for all transactions and aggregates them.

One of the things i wanted to achieve is constant processing time. In my understanding(i could be wrong) that can only be achieved by restricting
the calculation to a collection of constant size. 

Since i know i can only have 60 seconds of aggregates, i'd call that constant enough.

The memory footprint should be fine, as there's a constant amount of objects on the heap (60 in each array, yes, each, read on), and majority of the objects that come in can be quickly GCed.

The approach i used to limit everything to 60 seconds was to re-use a concept of a tumbling window. This allows me to dilute my data, and
to make sure that no transactions with future timestamps, or ones that are retarded enter my aggregates.

The data is spread between 60 equal parts. Each part contains an aggregator for a single second's worth of data.

To ensure that i don't have overlaps in my calculations, i'm keeping two arrays:
1) the array of aggregators
2) the array of timestamps narrowed down to a second.

Choosing which aggregator is done in a very simple way. Let's say a trx with a timestamp 12h34m14s130ms comes in. The StatsService
tries to find an aggregator which belongs to that timestamp. Initially it doesn't exist, so it creates entries in both arrays(aggregators and timestamps) 
at index 13(that's the seconds portion of the timestamp). Next event comes in at 12h34m14s131ms. StatsService finds the aggregator by
checking the seconds timestamp in the timestamps array, and uses that index to get the aggregator from the aggregators array, and adds the incoming
transaction to the mix.

You get my point... if an event comes in at XhYm34sZm, the StatsService will do the same process with finding the timestamped index and by that index it'll get the aggregator.

When at some point(like 12h35m14s130) a transaction comes in. The StatsService will look up the seconds timestamp in the timestamps array by index 14.
The fetched timestamp will not match the new one(the previous one was 12h34m14s130) and that will tell our service to close that single window and re-set the aggregator.
The next trx that falls into the same window will then be aggregated correctly.

I went for the simplest solution, as the spec had no mentioning of persistence or running the service in a distributed environment. 
