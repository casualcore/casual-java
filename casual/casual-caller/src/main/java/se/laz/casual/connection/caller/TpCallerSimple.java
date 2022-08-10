package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.logic.ConnectionFactoryMatcher;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualRequestInfo;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.connection.CasualConnectionException;

import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TpCallerSimple implements TpCaller
{
    private static final Logger LOG = Logger.getLogger(TpCallerSimple.class.getName());
    private ConnectionFactoryEntryStore connectionFactoryProvider;
    private SimpleCache simpleCache;
    private ConnectionFactoryMatcher connectionFactoryMatcher;

    // for wls
    public TpCallerSimple()
    {}

    @Inject
    public TpCallerSimple(ConnectionFactoryEntryStore connectionFactoryProvider, SimpleCache simpleCache, ConnectionFactoryMatcher connectionFactoryMatcher)
    {
        this.connectionFactoryProvider = connectionFactoryProvider;
        this.simpleCache = simpleCache;
        this.connectionFactoryMatcher = connectionFactoryMatcher;
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
        // TODO:
        // For caching, if there, for a pool, are new domain ids available
        // we need to issue lookup for the service name for those domain ids
        // For domain ids that are not currently available currently, we will remove them
        // For domain discovery for the newly available domain id(s) we can issue a discovery of the whole set of known services/queues + this serviceName
        Map<ConnectionFactoryEntry, List<DomainId>> poolDomainIds = getPoolDomainIds(connectionFactoryProvider.get());
        ServiceInfo serviceInfo = ServiceInfo.of(serviceName);
        List<MatchingEntry> matchingEntries = simpleCache.get(serviceInfo);
        boolean emptyBeforeMatching = matchingEntries.isEmpty();
        if(emptyBeforeMatching)
        {
            matchingEntries = connectionFactoryMatcher.matchService(serviceInfo, connectionFactoryProvider.get(), poolDomainIds);
            simpleCache.store(serviceInfo, matchingEntries);
        }
        if(matchingEntries.isEmpty())
        {
            // TODO: nothing supports service - TPENOENT or should we throw?
            return tpenoentSupplier.get();
        }
        return makeServiceCall(matchingEntries, serviceName, callFunction);
    }

    private <R> R makeServiceCall(List<MatchingEntry> matchingEntries, String serviceName, Function<CasualConnection, R> function)
    {
        Exception thrownException = null;
        for(MatchingEntry entry : matchingEntries)
        {
            ConnectionRequestInfo requestInfo = CasualRequestInfo.of(entry.getDomainId());
            try(CasualConnection connection = entry.getConnectionFactoryEntry().getConnectionFactory().getConnection(requestInfo))
            {
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
        String matchingEntriesAsText = matchingEntries.stream()
                                                      .map(entry -> entry.toString())
                                                      .collect(Collectors.joining(","));
        throw new CasualResourceException("Call failed to all " + matchingEntries.size() + " matching casual connections.\n" + matchingEntriesAsText, thrownException);
    }

    private ServiceReturn<CasualBuffer> tpenoentReply()
    {
        return new ServiceReturn<>(ServiceBuffer.empty(), ServiceReturnState.TPFAIL, ErrorState.TPENOENT, 0L);
    }

    private Map<ConnectionFactoryEntry, List<DomainId>> getPoolDomainIds(List<ConnectionFactoryEntry> connectionFactoryEntries)
    {
        Map<ConnectionFactoryEntry, List<DomainId>> result = new HashMap<>();
        for(ConnectionFactoryEntry connectionFactoryEntry : connectionFactoryEntries)
        {
            List<DomainId> entries = getPoolDomainIds(connectionFactoryEntry);
            result.putIfAbsent(connectionFactoryEntry, new ArrayList<>());
            result.get(connectionFactoryEntry).addAll(entries);
        }
        return result;
    }

    private List<DomainId> getPoolDomainIds(ConnectionFactoryEntry connectionFactoryEntry)
    {
        try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection())
        {
            return connection.getPoolDomainIds();
        }
        catch (ResourceException e)
        {
            // NOP
        }
        // we ignore this since it may be that the pool is currently unavailable
        return Collections.emptyList();
    }
}
