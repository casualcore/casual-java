/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.conversation;

import se.laz.casual.api.CasualConversationAPI;
import se.laz.casual.api.Conversation;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.conversation.Duplex;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.jca.ConversationConnectException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.conversation.ConnectReply;
import se.laz.casual.network.protocol.messages.conversation.ConnectRequest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ConversationConnectCaller implements CasualConversationAPI
{
    private static final Logger LOG = Logger.getLogger(ConversationConnectCaller.class.getName());
    private static final int RESULT_CODE_UNKNOWN = -1;
    private final CasualManagedConnection managedConnection;

    private ConversationConnectCaller(CasualManagedConnection managedConnection)
    {
        this.managedConnection = managedConnection;
    }

    public static ConversationConnectCaller of(final CasualManagedConnection managedConnection)
    {
        Objects.requireNonNull(managedConnection, "managedConnection can not be null");
        return new ConversationConnectCaller(managedConnection);
    }

    @Override
    public Conversation tpconnect(String serviceName, Optional<CasualBuffer> data, Flag<AtmiFlags> flags)
    {
        Objects.requireNonNull(serviceName, "serviceName can not be null");
        Objects.requireNonNull(data, "data can not be null");
        Objects.requireNonNull(flags, "flags can not be null");
        ConversationDirection conversationDirection = getDirection(flags);
        Duration timeout = Duration.of(managedConnection.getTransactionTimeout(), ChronoUnit.SECONDS);
        UUID conversationExecution = UUID.randomUUID();
        ConnectRequest.ConnectRequestBuilder connectRequestBuilder = ConnectRequest.createBuilder()
                .setExecution(conversationExecution)
                .setServiceName(serviceName)
                .setTimeout(timeout.toNanos())
                .setXid(managedConnection.getCurrentXid()).setDuplex(conversationDirection.isReceive() ? Duplex.SEND : Duplex.RECEIVE);
        data.ifPresent( buffer -> connectRequestBuilder.setServiceBuffer(ServiceBuffer.of(buffer)));
        ConnectRequest connectRequest = connectRequestBuilder.build();
        final UUID corrId = UUID.randomUUID();
        CasualNWMessage<ConnectRequest> connectNetworkMessage = CasualNWMessageImpl.of(corrId, connectRequest);
        CompletableFuture<CasualNWMessage<ConnectReply>> f = new CompletableFuture<>();
        CompletableFuture<CasualNWMessage<ConnectReply>> connectReplyFuture = managedConnection.getNetworkConnection().request(connectNetworkMessage);
        connectReplyFuture.whenComplete((v, e) ->{
            if(null != e)
            {
                LOG.finest(()->"conversation tpconnect failed for corrid: " + corrId + "\n serviceName" + serviceName);
                f.completeExceptionally(e);
                return;
            }
            LOG.finest(()->"conversation tpconnect ok for corrid: " + corrId + "\n serviceName" + serviceName);
            f.complete(v);
        });
        CasualNWMessage<ConnectReply> msg = f.join();
        ConnectReply reply = msg.getMessage();
        // result code is optional, RESULT_CODE_UNKNOWN means it is not available in this context
        if(reply.getResultCode() != RESULT_CODE_UNKNOWN)
        {
            ErrorState errorState = ErrorState.unmarshal(reply.getResultCode());
            if (errorState != ErrorState.OK)
            {
                throw new ConversationConnectException("tpconnect failed with " + errorState + " for request: " + connectRequest);
            }
        }
        return CasualConversationImpl.of(managedConnection, conversationDirection, corrId, conversationExecution);
    }

    private ConversationDirection getDirection(Flag<AtmiFlags> flags)
    {
        if(flags.isSet(AtmiFlags.TPRECVONLY))
        {
            return ConversationDirection.RECEIVE;
        }
        if(flags.isSet(AtmiFlags.TPSENDONLY))
        {
            return ConversationDirection.SEND;
        }
        throw new ConversationConnectException("Neither TPRECVONLY nor TPSENDONLY is set, this is an error!");
    }

}
