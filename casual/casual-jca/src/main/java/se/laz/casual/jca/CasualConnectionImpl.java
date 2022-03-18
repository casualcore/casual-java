/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.Conversation;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.api.service.ServiceDetails;
import se.laz.casual.jca.conversation.ConversationConnectCaller;
import se.laz.casual.jca.queue.CasualQueueCaller;
import se.laz.casual.jca.service.CasualServiceCaller;
import se.laz.casual.network.connection.CasualConnectionException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * CasualConnectionImpl - handle object for a connection
 * Managed by a ManagedConnection
 * @see CasualManagedConnection
 * @version $Revision: $
 */
public class CasualConnectionImpl implements CasualConnection
{
    private CasualServiceCaller serviceCaller;
    private CasualQueueCaller queueCaller;
    private ConversationConnectCaller conversationConnectCaller;
    private CasualManagedConnection managedConnection;

    /**
     * Create a connection handle with a reference to the underlying managed connection
     * created by the Application Server.
     *
     * @param mc  CasualManagedConnection
     */
    public CasualConnectionImpl(CasualManagedConnection mc)
    {
        this.managedConnection = mc;
        queueCaller = CasualQueueCaller.of(mc);
    }

    /**
     * Invalidate this connection handle removing its reference to
     * the underlying {@link javax.resource.spi.ManagedConnection}.
     */
    public void invalidate()
    {
        managedConnection = null;
    }

    /**
     * Is this connection handle valid i.e. associated with a managed connection.
     *
     * @return invalid true or valid false.
     */
    public boolean isInvalid()
    {
        return null == managedConnection;
    }

    @Override
    public void close()
    {
        throwIfInvalidated();
        managedConnection.closeHandle(this);
    }

    @Override
    public ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        throwIfInvalidated();
        return getCasualServiceCaller().tpcall( serviceName, data, flags);
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags)
    {
        throwIfInvalidated();
        return getCasualServiceCaller().tpacall( serviceName, data, flags);
    }

    @Override
    public boolean serviceExists(String serviceName)
    {
        throwIfInvalidated();
        return getCasualServiceCaller().serviceExists(serviceName);
    }

    @Override
    public List<ServiceDetails> serviceDetails(String serviceName)
    {
        return getCasualServiceCaller().serviceDetails(serviceName);
    }

    @Override
    public UUID enqueue(QueueInfo qinfo, QueueMessage msg)
    {
        throwIfInvalidated();
        return getCasualQueueCaller().enqueue(qinfo, msg);
    }

    @Override
    public List<QueueMessage> dequeue(QueueInfo qinfo, MessageSelector selector)
    {
        throwIfInvalidated();
        return getCasualQueueCaller().dequeue(qinfo, selector);
    }

    @Override
    public boolean queueExists(QueueInfo qinfo)
    {
        throwIfInvalidated();
        return getCasualQueueCaller().queueExists(qinfo);
    }

    private void throwIfInvalidated()
    {
        if(isInvalid())
        {
            throw new CasualConnectionException("connection is invalidated!");
        }
    }

    /**
     * Get the {@link CasualManagedConnection} to which this handle refers.
     *
     * @return current reference managed connection or null, if invalidated.
     */
    public CasualManagedConnection getManagedConnection()
    {
        return managedConnection;
    }

    /**
     * Set the {@link CasualManagedConnection} to which this handle refers.
     *
     * @param managedConnection managed connection to which this refers.
     */
    public void setManagedConnection(CasualManagedConnection managedConnection)
    {
        this.managedConnection = managedConnection;
    }

    @Override
    public Conversation tpconnect(String serviceName, Optional<CasualBuffer> data, Flag<AtmiFlags> flags)
    {
        Objects.requireNonNull(serviceName,"serviceName can not be null");
        Objects.requireNonNull(data, "data can not be null");
        Objects.requireNonNull(flags, "flags can not be null");
        return getConversationConnectCaller().tpconnect(serviceName, data, flags);
    }

    private ConversationConnectCaller getConversationConnectCaller()
    {
        if ( conversationConnectCaller == null )
        {
            conversationConnectCaller = ConversationConnectCaller.of(getManagedConnection());
        }
        return conversationConnectCaller;
    }

    CasualServiceCaller getCasualServiceCaller()
    {
        if( serviceCaller == null )
        {
            return CasualServiceCaller.of( getManagedConnection() );
        }
        return serviceCaller;
    }

    CasualQueueCaller getCasualQueueCaller()
    {
        return queueCaller;
    }


    void setCasualServiceCaller( CasualServiceCaller serviceCaller )
    {
        this.serviceCaller  = serviceCaller;
    }



    @Override
    public String toString()
    {
        return "CasualConnectionImpl{" +
                "managedConnection=" + managedConnection +
                '}';
    }

}
