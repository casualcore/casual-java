package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.entities.MatchingEntry;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualRequestInfo;
import se.laz.casual.network.connection.CasualConnectionException;

import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TpCallerImpl implements TpCaller
{
    private static final Logger LOG = Logger.getLogger(TpCallerImpl.class.getName());
    private Cache cache;
    private PoolMatcher poolMatcher;
    private PoolManager poolManager;

    // for wls
    public TpCallerImpl()
    {}

    @Inject
    public TpCallerImpl(PoolMatcher poolMatcher, Cache cache, PoolManager poolManager)
    {
        this.cache = cache;
        this.poolMatcher = poolMatcher;
        this.poolManager = poolManager;
    }

    @Override
    public ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        LOG.finest(() -> "tpcall<" + serviceName + ">");
        return doCall(serviceName, connection -> connection.tpcall(serviceName, data, flags), () -> tpenoentReply());
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        LOG.finest(() -> "tpacall<" + serviceName + ">");
        return doCall(serviceName, connection -> connection.tpacall(serviceName, data, flags), () -> CompletableFuture.supplyAsync(this::tpenoentReply));
    }

    @Override
    public boolean serviceExist(ServiceInfo serviceInfo)
    {
        return !poolMatcher.match(serviceInfo, poolManager.getPools()).isEmpty();
    }

    private <R> R doCall(String serviceName,  Function<CasualConnection, R> callFunction, Supplier<R> tpenoentSupplier)
    {
        ServiceInfo serviceInfo = ServiceInfo.of(serviceName);
        List<CacheEntry> cacheEntries = cache.get(serviceInfo);
        if(cacheEntries.isEmpty())
        {
            List<MatchingEntry> matchingEntries = poolMatcher.match(serviceInfo, poolManager.getPools());
            cache.store(matchingEntries);
            cacheEntries = cache.get(serviceInfo);
            if(cacheEntries.isEmpty())
            {
                return tpenoentSupplier.get();
            }
        }
        return makeServiceCall(cacheEntries, serviceName, callFunction);
    }

    private <R> R makeServiceCall(List<CacheEntry> matchingEntries, String serviceName, Function<CasualConnection, R> function)
    {
        List<CacheEntry> entries = matchingEntries.stream().collect(Collectors.toList());
        Exception thrownException = null;
        Collections.shuffle(entries);
        for(CacheEntry entry : entries)
        {
            ConnectionRequestInfo requestInfo = CasualRequestInfo.of(entry.getDomainId());
            try(CasualConnection connection = entry.getConnectionFactoryEntry().getConnectionFactory().getConnection(requestInfo))
            {
                LOG.finest(() -> "service call to: " + serviceName + " using: " + entry);
                return function.apply(connection);
            }
            catch (CasualConnectionException e)
            {
                // These exceptions are rollback-only, do not attempt any retries.
                throw new CasualResourceException("Call failed during execution to service=" + serviceName + " on matching entry: " + entry + " because of a network connection error, retries not possible.", e);
            }
            catch (ResourceException e)
            {
                // log but continue trying with next match
                LOG.warning(() -> "failed calling service: " + serviceName + " using matching entry: " + entry + "\n" + e);
                thrownException = e;
            }
        }
        String matchingEntriesAsText = entries.stream()
                                              .map(entry -> entry.toString())
                                              .collect(Collectors.joining(","));
        throw new CasualResourceException("Call failed to all " + matchingEntries.size() + " matching casual connections.\n" + matchingEntriesAsText, thrownException);
    }

    private ServiceReturn<CasualBuffer> tpenoentReply()
    {
        return new ServiceReturn<>(ServiceBuffer.empty(), ServiceReturnState.TPFAIL, ErrorState.TPENOENT, 0L);
    }

}
