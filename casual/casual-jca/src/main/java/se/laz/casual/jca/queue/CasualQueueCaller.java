/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.queue;

import se.laz.casual.api.CasualQueueApi;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.queue.DequeueReturn;
import se.laz.casual.api.queue.EnqueueReturn;
import se.laz.casual.api.queue.MessageSelector;
import se.laz.casual.api.queue.QueueErrorCode;
import se.laz.casual.api.queue.QueueInfo;
import se.laz.casual.api.queue.QueueMessage;
import se.laz.casual.api.util.Pair;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.network.connection.CasualConnectionException;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3;

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
    public EnqueueReturn enqueue(QueueInfo qinfo, QueueMessage msg)
    {
        try
        {
            // Always setting error state OK for now. In the future when error state is handled in the casual queue
            // protocol any error state supplied from casual should be used (same with dequeue)
            // This future thing should be removed in some release, however it means a new major release
            // since it will break our already released API
            CasualEnqueueReplyMessage replyMessage = makeEnqueueCall(UUID.randomUUID(), qinfo, msg);
            EnqueueReturn.Builder builder = EnqueueReturn.createBuilder();
            builder.withErrorState(ErrorState.OK)
                   .withId(replyMessage.getId());
            if( connection.getNetworkConnection().getProtocolVersion().isGreaterThanOrEqualTo( VERSION_1_3 ) )
            {
                builder.withErrorCode(replyMessage.getCode());
            }
            return builder.build();
        }
        catch(Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    @Override
    public DequeueReturn dequeue(QueueInfo qinfo, MessageSelector selector)
    {
        try
        {
            // Always setting error state OK for now. In the future when error state is handled in the casual queue
            // protocol any error state supplied from casual should be used (same with enqueue)
            // This future thing should be removed in some release, however it means a new major release
            // since it will break our already released API
            Pair<Optional<QueueMessage>, Optional<QueueErrorCode>> answer = makeDequeueCall(UUID.randomUUID(), qinfo, selector);
            DequeueReturn.Builder builder = DequeueReturn.createBuilder();
            builder.withErrorState(ErrorState.OK);
            answer.first().ifPresent(builder::withQueueMessage);
            answer.second().ifPresent(builder::withErrorCode);
            return builder.build();
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
            return queueExists(UUID.randomUUID(), qinfo.getQueueName());
        }
        catch(Exception e)
        {
            throw new CasualConnectionException(e);
        }
    }

    private CasualEnqueueReplyMessage makeEnqueueCall(UUID corrid, QueueInfo qinfo, QueueMessage msg)
    {
        CasualEnqueueRequestMessage requestMessage = CasualEnqueueRequestMessage.createBuilder()
                                                                                .withExecution(UUID.randomUUID())
                                                                                .withXid(connection.getCurrentXid())
                                                                                .withQueueName(qinfo.getQueueName())
                                                                                .withMessage(EnqueueMessage.of(msg))
                                                                                .build();
        CasualNWMessage<CasualEnqueueRequestMessage> networkRequestMessage = CasualNWMessageImpl.of(corrid, requestMessage);
        CompletableFuture<CasualNWMessage<CasualEnqueueReplyMessage>> networkReplyMessageFuture = connection.getNetworkConnection().request(networkRequestMessage);

        CasualNWMessage<CasualEnqueueReplyMessage> networkReplyMessage = networkReplyMessageFuture.join();
        return networkReplyMessage.getMessage();
    }

    private Pair<Optional<QueueMessage>, Optional<QueueErrorCode>> makeDequeueCall(UUID corrid, QueueInfo qinfo, MessageSelector selector)
    {
        CasualDequeueRequestMessage requestMessage = CasualDequeueRequestMessage.createBuilder()
                                                                                .withExecution(UUID.randomUUID())
                                                                                .withXid(connection.getCurrentXid())
                                                                                .withQueueName(qinfo.getQueueName())
                                                                                .withSelectorProperties(selector.getSelector())
                                                                                .withSelectorUUID(selector.getSelectorId())
                                                                                .withBlock(qinfo.getOptions().isBlocking())
                                                                                .build();
        CasualNWMessage<CasualDequeueRequestMessage> networkRequestMessage = CasualNWMessageImpl.of(corrid, requestMessage);
        CompletableFuture<CasualNWMessage<CasualDequeueReplyMessage>> networkReplyMessageFuture = connection.getNetworkConnection().request(networkRequestMessage);

        CasualNWMessage<CasualDequeueReplyMessage> networkReplyMessage = networkReplyMessageFuture.join();
        CasualDequeueReplyMessage replyMessage = networkReplyMessage.getMessage();
        List<QueueMessage> messages = Transformer.transform(replyMessage.getMessages());
        Optional<QueueMessage> maybeMessage = messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
        Optional<QueueErrorCode> maybeErrorCode = connection.getNetworkConnection().getProtocolVersion().isGreaterThanOrEqualTo( VERSION_1_3 )
                ? Optional.of(replyMessage.getCode()) : Optional.empty();
        return Pair.of(maybeMessage, maybeErrorCode);
    }

    private boolean queueExists( UUID corrid, String queueName)
    {
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_ID ).getId())
                                                                                            .setDomainName(ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_NAME ))
                                                                                            .setQueueNames(Arrays.asList(queueName))
                                                                                            .build();
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessageImpl.of(corrid, requestMsg);
        CompletableFuture<CasualNWMessage<CasualDomainDiscoveryReplyMessage>> replyMsgFuture = connection.getNetworkConnection().request(msg);

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = replyMsgFuture.join();
        return replyMsg.getMessage().getQueues().stream()
                .map(Queue::getName)
                .anyMatch(v -> v.equals(queueName));
    }
}
