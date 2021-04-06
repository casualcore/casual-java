/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.queue;

import com.google.protobuf.ByteString;
import se.laz.casual.api.CasualQueueApi;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.api.util.time.InstantUtil;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.grpc.MessageCreator;
import se.laz.casual.network.messages.CasualDequeueReply;
import se.laz.casual.network.messages.CasualDequeueRequest;
import se.laz.casual.network.messages.CasualDomainDiscoveryReply;
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest;
import se.laz.casual.network.messages.CasualEnqueueReply;
import se.laz.casual.network.messages.CasualEnqueueRequest;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;
import se.laz.casual.network.messages.Selector;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        try
        {
            return makeEnqueueCall(UUID.randomUUID(), qinfo, msg);
        }
        catch(Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public List<QueueMessage> dequeue(QueueInfo qinfo, MessageSelector selector)
    {
        try
        {
            return makeDequeueCall(UUID.randomUUID(), qinfo, selector);
        }
        catch(Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public boolean queueExists(QueueInfo qinfo)
    {
        try
        {
            return queueExists(UUID.randomUUID(), qinfo.getCompositeName());
        }
        catch(Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    private UUID makeEnqueueCall(UUID corrid, QueueInfo qinfo, QueueMessage msg)
    {
        CasualEnqueueRequest requestMessage = CasualEnqueueRequest.newBuilder()
                                                                  .setExecution(MessageCreator.toUUID4(UUID.randomUUID()))
                                                                  .setXid(MessageCreator.toXID(connection.getCurrentXid()))
                                                                  .setQueueName(qinfo.getCompositeName())
                                                                  .setMessage(createQueueMessage(msg))
                                                                  .build();

        CasualRequest requestEnvelope = CasualRequest.newBuilder()
                                                     .setMessageType(CasualRequest.MessageType.ENQUEUE_REQUEST)
                                                     .setCorrelationId(MessageCreator.toUUID4(corrid))
                                                     .setEnqueue(requestMessage)
                                                     .build();
        CompletableFuture<CasualReply> networkReplyMessageFuture = connection.getNetworkConnection().request(requestEnvelope);

        CasualReply networkReplyMessage = networkReplyMessageFuture.join();
        CasualEnqueueReply replyMessage = networkReplyMessage.getEnqueue();
        return MessageCreator.toUUID(replyMessage.getMessageId());
    }

    private se.laz.casual.network.messages.QueueMessage createQueueMessage(QueueMessage msg)
    {
        se.laz.casual.network.messages.QueueMessage queueMessage = se.laz.casual.network.messages.QueueMessage.newBuilder()
                                                                                                              .setId(MessageCreator.toUUID4(msg.getId()))
                                                                                                              .setType(msg.getPayload().getType())
                                                                                                              .setProperties(msg.getCorrelationInformation())
                                                                                                              .setReplyQueue(msg.getReplyQueue())
                                                                                                              .setAvailableSince(InstantUtil.toNanos(msg.getAvailableSince()))
                                                                                                              .setPayload(ByteString.copyFrom(msg.getPayload().getBytes().get(0)))
                                                                                                              .build();
        return queueMessage;
    }

    private List<QueueMessage> makeDequeueCall(UUID corrid, QueueInfo qinfo, MessageSelector selector)
    {
        CasualDequeueRequest requestMessage = CasualDequeueRequest.newBuilder()
                                                                  .setExecution(MessageCreator.toUUID4(UUID.randomUUID()))
                                                                  .setXid(MessageCreator.toXID(connection.getCurrentXid()))
                                                                  .setQueueName(qinfo.getCompositeName())
                                                                  .setSelector(Selector.newBuilder()
                                                                                       .setId(MessageCreator.toUUID4(selector.getSelectorId()))
                                                                                       .setProperties(selector.getSelector())
                                                                                       .build())
                                                                  .setBlock(qinfo.getOptions().isBlocking())
                                                                  .build();


        CasualRequest requestEnvelope = CasualRequest.newBuilder()
                                                     .setMessageType(CasualRequest.MessageType.DEQUEUE_REQUEST)
                                                     .setCorrelationId(MessageCreator.toUUID4(corrid))
                                                     .setDequeue(requestMessage)
                                                     .build();

        CompletableFuture<CasualReply> networkReplyMessageFuture = connection.getNetworkConnection().request(requestEnvelope);

        CasualReply networkReplyMessage = networkReplyMessageFuture.join();
        CasualDequeueReply replyMessage = networkReplyMessage.getDequeue();
        return Transformer.transform(replyMessage);
    }

    private boolean queueExists( UUID corrid, String queueName)
    {
        CasualDomainDiscoveryRequest requestMsg = CasualDomainDiscoveryRequest.newBuilder()
                                                                              .setExecution(MessageCreator.toUUID4(UUID.randomUUID()))
                                                                              .setDomainId(MessageCreator.toUUID4(UUID.randomUUID()))
                                                                              .setDomainName(connection.getDomainName())
                                                                              .addAllQueueNames(Arrays.asList(queueName))
                                                                              .build();
        CasualRequest requestEnvelope = CasualRequest.newBuilder()
                                                     .setMessageType(CasualRequest.MessageType.DOMAIN_DISCOVERY_REQUEST)
                                                     .setCorrelationId(MessageCreator.toUUID4(corrid))
                                                     .setDomainDiscovery(requestMsg)
                                                     .build();

        CompletableFuture<CasualReply> replyMsgFuture = connection.getNetworkConnection().request(requestEnvelope);

        CasualReply replyMsg = replyMsgFuture.join();
        CasualDomainDiscoveryReply reply = replyMsg.getDomainDiscovery();
        return reply.getQueuesList().stream()
                    .map(q -> q.getName())
                    .anyMatch(v -> v.equals(queueName));
    }
}
