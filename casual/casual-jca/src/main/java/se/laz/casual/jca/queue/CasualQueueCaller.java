/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.queue;

import se.laz.casual.api.CasualQueueApi;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.jca.CasualResourceAdapterException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.domain.Queue;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueReplyMessage;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueReplyMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueRequestMessage;
import se.laz.casual.network.protocol.messages.queue.EnqueueMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CasualQueueCaller implements CasualQueueApi
{
    private CasualManagedConnection connection;

    private CasualQueueCaller(final CasualManagedConnection connection)
    {
        this.connection = connection;
    }

    public static CasualQueueCaller of(final CasualManagedConnection connection)
    {
        Objects.requireNonNull(connection);
        return new CasualQueueCaller(connection);
    }

    @Override
    public UUID enqueue(QueueInfo qinfo, QueueMessage msg)
    {
        return makeEnqueueCall(UUID.randomUUID(), qinfo, msg);
    }

    @Override
    public List<QueueMessage> dequeue(QueueInfo qinfo, MessageSelector selector)
    {
        return makeDequeueCall(UUID.randomUUID(), qinfo, selector);
    }

    @Override
    public boolean queueExists(QueueInfo qinfo)
    {
        return queueExists( UUID.randomUUID(), qinfo.getCompositeName());
    }

    private UUID makeEnqueueCall(UUID corrid, QueueInfo qinfo, QueueMessage msg)
    {
        CasualEnqueueRequestMessage requestMessage = CasualEnqueueRequestMessage.createBuilder()
                                                                                .withExecution(UUID.randomUUID())
                                                                                .withXid(connection.getCurrentXid())
                                                                                .withQueueName(qinfo.getCompositeName())
                                                                                .withMessage(EnqueueMessage.of(msg))
                                                                                .build();
        CasualNWMessage<CasualEnqueueRequestMessage> networkRequestMessage = CasualNWMessageImpl.of(corrid, requestMessage);
        CompletableFuture<CasualNWMessage<CasualEnqueueReplyMessage>> networkReplyMessageFuture = connection.getNetworkConnection().request(networkRequestMessage);
        try
        {
            CasualNWMessage<CasualEnqueueReplyMessage> networkReplyMessage = networkReplyMessageFuture.get();
            CasualEnqueueReplyMessage replyMessage = networkReplyMessage.getMessage();
            return replyMessage.getId();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualResourceAdapterException(e);
        }
    }

    private List<QueueMessage> makeDequeueCall(UUID corrid, QueueInfo qinfo, MessageSelector selector)
    {
        CasualDequeueRequestMessage requestMessage = CasualDequeueRequestMessage.createBuilder()
                                                                                .withExecution(UUID.randomUUID())
                                                                                .withXid(connection.getCurrentXid())
                                                                                .withQueueName(qinfo.getCompositeName())
                                                                                .withSelectorProperties(selector.getSelector())
                                                                                .withSelectorUUID(selector.getSelectorId())
                                                                                .withBlock(false)
                                                                                .build();
        CasualNWMessage<CasualDequeueRequestMessage> networkRequestMessage = CasualNWMessageImpl.of(corrid, requestMessage);
        CompletableFuture<CasualNWMessage<CasualDequeueReplyMessage>> networkReplyMessageFuture = connection.getNetworkConnection().request(networkRequestMessage);
        try
        {
            CasualNWMessage<CasualDequeueReplyMessage> networkReplyMessage = networkReplyMessageFuture.get();
            CasualDequeueReplyMessage replyMessage = networkReplyMessage.getMessage();
            return Transformer.transform(replyMessage.getMessages());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualResourceAdapterException(e);
        }
    }

    private boolean queueExists( UUID corrid, String queueName)
    {
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(UUID.randomUUID())
                                                                                            .setDomainName( connection.getDomainName() )
                                                                                            .setQueueNames(Arrays.asList(queueName))
                                                                                            .build();
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessageImpl.of(corrid, requestMsg);
        CompletableFuture<CasualNWMessage<CasualDomainDiscoveryReplyMessage>> replyMsgFuture = connection.getNetworkConnection().request(msg);
        try
        {
            CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = replyMsgFuture.get();
            return replyMsg.getMessage().getQueues().stream()
                           .map(Queue::getName)
                           .anyMatch(v -> v.equals(queueName));
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualResourceAdapterException(e);
        }
    }
}
