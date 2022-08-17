/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.entities.MatchingEntry;
import se.laz.casual.connection.caller.entities.Pool;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualConnectionListener;
import se.laz.casual.jca.CasualRequestInfo;
import se.laz.casual.jca.DomainId;

import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TransactionLess
{
    private TransactionManager transactionManager;

    // NOP constructor needed for WLS
    public TransactionLess()
    {}

    @Inject
    public TransactionLess(TransactionManagerProvider transactionManagerProvider)
    {
        transactionManager = transactionManagerProvider.getTransactionManager();
    }

    public ServiceReturn<CasualBuffer> tpcall(Supplier<ServiceReturn<CasualBuffer>> supplier)
    {
        try
        {
            Optional<Transaction> currentTransaction = Optional.ofNullable(transactionManager.suspend());
            ServiceReturn<CasualBuffer> value = supplier.get();
            currentTransaction.ifPresent(this::resumeTransaction);
            return value;
        }
        catch (SystemException e)
        {
            throw new CasualCallerException("Failed suspending current transaction", e);
        }
    }

    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(Supplier<CompletableFuture<ServiceReturn<CasualBuffer>>> supplier)
    {
        try
        {
            Optional<Transaction> currentTransaction = Optional.ofNullable(transactionManager.suspend());
            CompletableFuture<ServiceReturn<CasualBuffer>> value = supplier.get();
            currentTransaction.ifPresent(this::resumeTransaction);
            return value;
        }
        catch (SystemException e)
        {
            throw new CasualCallerException("Failed suspending current transaction", e);
        }
    }

    private void resumeTransaction(Transaction transaction)
    {
        try
        {
            transactionManager.resume(transaction);
        }
        catch (InvalidTransactionException | SystemException e)
        {
            throw new CasualCallerException("Failed resuming transaction", e);
        }
    }

    public static class PoolDataRetriever
    {
        public List<Pool> get(List<ConnectionFactoryEntry> connectionFactoryEntries)
        {
            return get(connectionFactoryEntries, null);
        }

        public List<Pool> get(List<ConnectionFactoryEntry> connectionFactoryEntries, CasualConnectionListener connectionListener)
        {
            List<Pool> pools = new ArrayList<>();
            for(ConnectionFactoryEntry connectionFactoryEntry : connectionFactoryEntries)
            {
                get(connectionFactoryEntry, connectionListener).ifPresent(pool -> pools.add(pool));
            }
            return pools;
        }

        private Optional<Pool> get(ConnectionFactoryEntry connectionFactoryEntry, CasualConnectionListener connectionListener)
        {
            try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection())
            {
                if(null != connectionListener)
                {
                    connection.addConnectionListener(connectionListener);
                }
                return  Optional.of(Pool.of(connectionFactoryEntry, connection.getPoolDomainIds()));
            }
            catch (ResourceException e)
            {
                // NOP
                // we ignore this since it may be that the pool is currently unavailable
            }
            return Optional.empty();
        }
    }

    public static class PoolMatcher
    {
        private static final Logger LOG = Logger.getLogger(PoolMatcher.class.getName());

        public List<MatchingEntry> match(ServiceInfo serviceInfo, List<Pool> pools)
        {
            return match(Arrays.asList(serviceInfo), Collections.emptyList(), pools);
        }

        public List<MatchingEntry> match(QueueInfo queueInfo, List<Pool> pools)
        {
            return match(Collections.emptyList(), Arrays.asList(queueInfo), pools);
        }

        public List<MatchingEntry> match(List<ServiceInfo> services, List<QueueInfo> queues,  List<Pool> pools)
        {
            List<MatchingEntry> matchingEntries = new ArrayList<>();
            CasualRequestInfo requestInfo = CasualRequestInfo.of(services, queues);
            LOG.warning(() -> "requestinfo: " + requestInfo);
            pools.forEach(pool -> {
                List<MatchingEntry> maybeMatching = matches(requestInfo, pool.getConnectionFactoryEntry(), pool.getDomainIds());
                matchingEntries.addAll(maybeMatching);
            });
            String entriesString = matchingEntries.stream()
                                                  .map(v -> v.toString())
                                                  .collect(Collectors.joining(","));
            LOG.warning(() -> "# of matching entries: " + matchingEntries.size() + "\n values: " + entriesString);
            return matchingEntries;
        }

        //Note: due to try with resources usage where we never use the resource
        @SuppressWarnings("try")
        private List<MatchingEntry> matches(CasualRequestInfo requestInfo, ConnectionFactoryEntry connectionFactoryEntry, List<DomainId> poolDomainIds)
        {
            List<MatchingEntry> entries = new ArrayList<>();
            for(DomainId domainId : poolDomainIds)
            {
                ConnectionRequestInfo domainIdRequestInfo = CasualRequestInfo.of(domainId);
                try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection(domainIdRequestInfo))
                {
                    DiscoveryReturn discoveryReturn = connection.discover(UUID.randomUUID(), requestInfo.getServices(), requestInfo.getQueues());
                    LOG.warning(() -> "discoveryReturn:" + discoveryReturn);
                    entries.add(MatchingEntry.of(connectionFactoryEntry, domainId, discoveryReturn.getServiceDetails(), discoveryReturn.getQueueDetails()));
                }
                catch (ResourceException resourceException)
                {
                    // NOP
                }
            }
            return entries;
        }


    }
}
