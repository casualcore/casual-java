package se.laz.casual.connection.caller;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.queue.DequeueReturn;
import se.laz.casual.api.queue.EnqueueReturn;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.connection.caller.entities.MatchingEntry;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualRequestInfo;

import javax.resource.spi.ConnectionRequestInfo;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

public class QueueCallerImpl implements QueueCaller
{
    private static final Logger LOG = Logger.getLogger(QueueCallerImpl.class.getName());
    private PoolMatcher poolMatcher;
    private Cache cache;
    private PoolManager poolManager;

    // For the wls
    public QueueCallerImpl()
    {}

    public QueueCallerImpl(PoolMatcher poolMatcher, Cache cache, PoolManager poolManager)
    {
        this.poolMatcher = poolMatcher;
        this.cache = cache;
        this.poolManager = poolManager;
    }

    @Override
    public EnqueueReturn enqueue(QueueInfo qinfo, QueueMessage msg)
    {
        MatchingEntry entry = cache.get(qinfo).orElse(null);
        if(null == entry)
        {
            List<MatchingEntry> matchingEntries = poolMatcher.match(qinfo, poolManager.getPools());
            cache.store(matchingEntries);
            if(matchingEntries.isEmpty())
            {
                return EnqueueReturn.createBuilder().withErrorState(ErrorState.TPENOENT).build();
            }
            entry = cache.get(qinfo).orElse(null);
        }
        return doCall(qinfo, entry, connection -> connection.enqueue(qinfo, msg), "enqueue");
    }

    @Override
    public DequeueReturn dequeue(QueueInfo qinfo, MessageSelector selector)
    {
        MatchingEntry entry = cache.get(qinfo).orElse(null);
        if(null == entry)
        {
            List<MatchingEntry> matchingEntries = poolMatcher.match(qinfo, poolManager.getPools());
            cache.store(matchingEntries);
            if(matchingEntries.isEmpty())
            {
                return DequeueReturn.createBuilder().withErrorState(ErrorState.TPENOENT).build();
            }
            entry = cache.get(qinfo).orElse(null);
        }
        return doCall(qinfo, entry, connection -> connection.dequeue(qinfo, selector), "dequeue");
    }

    private <R> R doCall(QueueInfo queueInfo, MatchingEntry entry, Function<CasualConnection, R> function, String callName)
    {
        ConnectionRequestInfo requestInfo = CasualRequestInfo.of(entry.getDomainId());
        try(CasualConnection connection = entry.getConnectionFactoryEntry().getConnectionFactory().getConnection(requestInfo))
        {
            LOG.finest(() -> callName + " call using: " + entry);
            return function.apply(connection);
        }
        catch (Exception e)
        {
            // can never retry since only ever match with one queue, they are unique - or should be at least
            throw new CasualResourceException(callName + " call failed for " + queueInfo + " on matching entry: " + entry + " retries not possible since queues are uniquely available.", e);
        }
    }
}
