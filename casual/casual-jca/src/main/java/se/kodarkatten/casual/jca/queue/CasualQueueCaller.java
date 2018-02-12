package se.kodarkatten.casual.jca.queue;

import se.kodarkatten.casual.api.CasualQueueApi;
import se.kodarkatten.casual.api.queue.MessageSelector;
import se.kodarkatten.casual.api.queue.QueueInfo;
import se.kodarkatten.casual.api.queue.QueueMessage;
import se.kodarkatten.casual.jca.CasualManagedConnection;
import se.kodarkatten.casual.jca.CasualResourceAdapterException;
import se.kodarkatten.casual.jca.service.Transformer;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.domain.Queue;
import se.kodarkatten.casual.network.messages.queue.CasualDequeueReplyMessage;
import se.kodarkatten.casual.network.messages.queue.CasualDequeueRequestMessage;
import se.kodarkatten.casual.network.messages.queue.CasualEnqueueReplyMessage;
import se.kodarkatten.casual.network.messages.queue.CasualEnqueueRequestMessage;
import se.kodarkatten.casual.network.messages.queue.EnqueueMessage;

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
        CasualNWMessage<CasualEnqueueRequestMessage> networkRequestMessage = CasualNWMessage.of(corrid, requestMessage);
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
        CasualNWMessage<CasualDequeueRequestMessage> networkRequestMessage = CasualNWMessage.of(corrid, requestMessage);
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
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessage.of(corrid, requestMsg);
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