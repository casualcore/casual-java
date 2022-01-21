/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.api.service.ServiceDetails;
import se.laz.casual.jca.CasualConnection;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Remote(CasualCaller.class)
@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class CasualCallerImpl implements CasualCaller
{
    private TpCallerFailover tpCaller = new TpCallerFailover();
    private ConnectionFactoryLookup lookup;
    private ConnectionFactoryProvider connectionFactoryProvider;
    private TransactionManager transactionManager;

    // NOP constructor needed for WLS
    public CasualCallerImpl()
    {}

    @Inject
    public CasualCallerImpl(ConnectionFactoryLookup lookup, ConnectionFactoryProvider connectionFactoryProvider, TransactionManagerProvider transactionManagerProvider)
    {
        this.lookup = lookup;
        this.connectionFactoryProvider = connectionFactoryProvider;
        this.transactionManager = transactionManagerProvider.getTransactionManager();
        List<ConnectionFactoryEntry> possibleEntries = connectionFactoryProvider.get();
        if(possibleEntries.isEmpty())
        {
            throw new CasualCallerException("No connection factories available, casual caller is not usable");
        }
    }

    @Override
    public ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        return flags.isSet(AtmiFlags.TPNOTRAN) ? tpcallNoTransaction(serviceName, data, flags) : tpCaller.tpcall(serviceName, data, flags, lookup);
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        return flags.isSet(AtmiFlags.TPNOTRAN) ? tpacallNoTransaction(serviceName, data, flags) : tpCaller.tpacall(serviceName, data, flags, lookup);
    }

    @Override
    public boolean serviceExists(String serviceName)
    {
        return !lookup.get(serviceName).isEmpty();
    }

    @Override
    public List<ServiceDetails> serviceDetails(String serviceName)
    {
        throw new CasualCallerException("CasualCaller does not support serviceDetails. Please use CasualServiceCaller::serviceDetails to get specific service details.");
    }

    @Override
    public UUID enqueue(QueueInfo qinfo, QueueMessage msg)
    {
        ConnectionFactoryEntry entry = RandomEntry.getEntry(lookup.get(qinfo)).orElse(RandomEntry.getRandomEntry(connectionFactoryProvider.get()));
        try(CasualConnection connection = entry.getConnectionFactory().getConnection())
        {
            return connection.enqueue(qinfo, msg);
        }
        catch (ResourceException e)
        {
            throw new CasualResourceException(e);
        }
    }

    @Override
    public List<QueueMessage> dequeue(QueueInfo qinfo, MessageSelector selector)
    {
        ConnectionFactoryEntry entry = RandomEntry.getEntry(lookup.get(qinfo)).orElse(RandomEntry.getRandomEntry(connectionFactoryProvider.get()));
        try(CasualConnection connection = entry.getConnectionFactory().getConnection())
        {
            return connection.dequeue(qinfo, selector);
        }
        catch (ResourceException e)
        {
            throw new CasualResourceException(e);
        }
    }

    @Override
    public boolean queueExists(QueueInfo qinfo)
    {
        return !lookup.get(qinfo).isEmpty();
    }

    private ServiceReturn<CasualBuffer> tpcallNoTransaction(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        try
        {
            Transaction currentTransaction = transactionManager.suspend();
            ServiceReturn<CasualBuffer> value = tpCaller.tpcall(serviceName, data, flags, lookup);
            transactionManager.resume(currentTransaction);
            return value;
        }
        catch (SystemException e)
        {
            throw new CasualCallerException("Failed suspending current transaction", e);
        }
        catch (InvalidTransactionException e)
        {
            throw new CasualCallerException("Failed resuming transaction", e);
        }
    }

    private CompletableFuture<ServiceReturn<CasualBuffer>> tpacallNoTransaction(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        try
        {
            Transaction currentTransaction = transactionManager.suspend();
            CompletableFuture<ServiceReturn<CasualBuffer>> value = tpCaller.tpacall(serviceName, data, flags, lookup);
            transactionManager.resume(currentTransaction);
            return value;
        }
        catch (SystemException e)
        {
            throw new CasualCallerException("Failed suspending current transaction", e);
        }
        catch (InvalidTransactionException e)
        {
            throw new CasualCallerException("Failed resuming transaction", e);
        }
    }



}
