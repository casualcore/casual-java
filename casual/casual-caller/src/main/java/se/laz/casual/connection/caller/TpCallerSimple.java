package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.logic.PoolMatcher;
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

public class TpCallerSimple implements TpCaller
{
    private static final Logger LOG = Logger.getLogger(TpCallerSimple.class.getName());
    private SimpleCache simpleCache;
    private PoolMatcher poolMatcher;
    private PoolManager poolManager;

    // for wls
    public TpCallerSimple()
    {}

    @Inject
    public TpCallerSimple(PoolMatcher poolMatcher, SimpleCache simpleCache, PoolManager poolManager)
    {
        this.simpleCache = simpleCache;
        this.poolMatcher = poolMatcher;
        this.poolManager = poolManager;
    }

    @Override
    public ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags, ConnectionFactoryLookup lookup)
    {
        LOG.warning(() -> "tpcall<" + serviceName + ">");
        return doCall(serviceName, connection -> connection.tpcall(serviceName, data, flags), () -> tpenoentReply());
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags, ConnectionFactoryLookup lookup)
    {
        LOG.warning(() -> "tpacall<" + serviceName + ">");
        return doCall(serviceName, connection -> connection.tpacall(serviceName, data, flags), () -> CompletableFuture.supplyAsync(this::tpenoentReply));
    }

    private <R> R doCall(String serviceName,  Function<CasualConnection, R> callFunction, Supplier<R> tpenoentSupplier)
    {
        ServiceInfo serviceInfo = ServiceInfo.of(serviceName);
        List<MatchingEntry> matchingEntries = simpleCache.get(serviceInfo);
        if(matchingEntries.isEmpty())
        {
            matchingEntries = poolMatcher.match(serviceInfo, poolManager.getPools());
            simpleCache.store(matchingEntries);
        }
        if(matchingEntries.isEmpty())
        {
            return tpenoentSupplier.get();
        }
        return makeServiceCall(matchingEntries, serviceName, callFunction);
    }

    private <R> R makeServiceCall(List<MatchingEntry> matchingEntries, String serviceName, Function<CasualConnection, R> function)
    {
        List<MatchingEntry> entries = matchingEntries.stream().collect(Collectors.toList());
        Exception thrownException = null;
        Collections.shuffle(entries);
        for(MatchingEntry entry : entries)
        {
            ConnectionRequestInfo requestInfo = CasualRequestInfo.of(entry.getDomainId());
            try(CasualConnection connection = entry.getConnectionFactoryEntry().getConnectionFactory().getConnection(requestInfo))
            {
                LOG.warning(() -> "service call using: " + entry);
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
