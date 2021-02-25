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
import se.laz.casual.jca.CasualConnection;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.resource.ResourceException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Remote(CasualCaller.class)
@Stateless
public class CasualCallerImpl implements CasualCaller
{
    private ConnectionFactoryLookup lookup;
    private ConnectionFactoryProvider connectionFactoryProvider;

    // NOP constructor needed for WLS
    public CasualCallerImpl()
    {}

    @Inject
    public CasualCallerImpl(ConnectionFactoryLookup lookup, ConnectionFactoryProvider connectionFactoryProvider)
    {
        this.lookup = lookup;
        this.connectionFactoryProvider = connectionFactoryProvider;
        List<ConnectionFactoryEntry> possibleEntries = connectionFactoryProvider.get();
        if(possibleEntries.isEmpty())
        {
            throw new CasualCallerException("No connection factories available, casual caller is not usable");
        }
    }

    @Override
    public ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        ConnectionFactoryEntry entry = RandomEntry.getEntry(lookup.get(serviceName)).orElse(RandomEntry.getRandomEntry(connectionFactoryProvider.get()));
        try(CasualConnection connection = entry.getConnectionFactory().getConnection())
        {
            return connection.tpcall(serviceName, data, flags);
        }
        catch (ResourceException e)
        {
            throw new CasualResourceException(e);
        }
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        ConnectionFactoryEntry entry = RandomEntry.getEntry(lookup.get(serviceName)).orElse(RandomEntry.getRandomEntry(connectionFactoryProvider.get()));
        try(CasualConnection connection = entry.getConnectionFactory().getConnection())
        {
            return connection.tpacall(serviceName, data, flags);
        }
        catch (ResourceException e)
        {
            throw new CasualResourceException(e);
        }
    }

    @Override
    public boolean serviceExists(String serviceName)
    {
        return !lookup.get(serviceName).isEmpty();
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



}
