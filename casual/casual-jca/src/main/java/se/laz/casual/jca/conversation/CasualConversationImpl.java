/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.conversation;

import se.laz.casual.api.Conversation;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ConversationReturn;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.conversation.Duplex;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.jca.CasualManagedConnection;
import se.laz.casual.jca.ConversationDirectionException;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.conversation.Disconnect;
import se.laz.casual.network.protocol.messages.conversation.Request;

import java.util.Objects;
import java.util.UUID;

public class CasualConversationImpl implements Conversation
{
    private static final int RESULT_CODE_UNKNOWN = -1;
    private final CasualManagedConnection managedConnection;
    private final UUID corrId;
    private ConversationDirection conversationDirection;
    private boolean directionSwitched;
    private final UUID execution;

    private CasualConversationImpl(CasualManagedConnection managedConnection, final ConversationDirection conversationDirection, UUID corrId, UUID execution)
    {
        this.managedConnection = managedConnection;
        this.conversationDirection = conversationDirection;
        this.corrId = corrId;
        this.execution = execution;
    }

    public static Conversation of(final CasualManagedConnection managedConnection, final ConversationDirection conversationDirection, final UUID corrId, UUID execution)
    {
        Objects.requireNonNull(managedConnection, "managedConnection can not be null");
        Objects.requireNonNull(conversationDirection, "conversationDirection can not be null");
        Objects.requireNonNull(corrId, "corrdId can not be null");
        Objects.requireNonNull(execution, "execution can not be null");
        return new CasualConversationImpl(managedConnection, conversationDirection, corrId, execution);
    }

    @Override
    public boolean isSending()
    {
        return conversationDirection.isSend();
    }

    @Override
    public boolean isReceiving()
    {
        return !isSending();
    }

    @Override
    public void tpdiscon()
    {
        Disconnect disconnect = Disconnect.createBuilder()
                                          .setExecution(execution)
                                          .build();
        CasualNWMessage<Disconnect> message = CasualNWMessageImpl.of(corrId, disconnect);
        managedConnection.getNetworkConnection().send(message);
    }

    @Override
    public ConversationReturn<CasualBuffer> tprecv()
    {
        if(conversationDirection.isSend())
        {
            throw new ConversationDirectionException("tprecv called but expecting tpsend");
        }
        directionSwitched = false;
        CasualNWMessage<Request> message = managedConnection.getNetworkConnection().receive(corrId).join();
        Request conversationReplyMessage = message.getMessage();
        maybeSwitchConversationDirectionOnRecv(conversationReplyMessage.getDuplex());
        return createConversationReturn(conversationReplyMessage);
    }

    @Override
    public void tpsend(CasualBuffer data, boolean handOverControl)
    {
        tpsend(data, handOverControl, Request.RequestBuilder.NO_RESULT_CODE);
    }

    @Override
    public void tpsend(CasualBuffer data, boolean handOverControl, long userCode)
    {
        Objects.requireNonNull(data, "data can not be null");
        if(conversationDirection.isReceive())
        {
            throw new ConversationDirectionException("tpsend called but expecting tprecv");
        }
        directionSwitched = false;
        if(handOverControl)
        {
            switchConversationDirection();
        }
        Request request =  Request.createBuilder()
                                  .setExecution(execution)
                                  .setDuplex(conversationDirection.isReceive() ? Duplex.SEND : Duplex.RECEIVE)
                                  .setServiceBuffer(ServiceBuffer.of(data))
                                  .setUserCode(userCode)
                                  .build();
        CasualNWMessage<Request> envelope = CasualNWMessageImpl.of(corrId, request);
        managedConnection.getNetworkConnection().send(envelope);
    }

    @Override
    public boolean isDirectionSwitched()
    {
        return directionSwitched;
    }

    private void switchConversationDirection()
    {
        conversationDirection = conversationDirection.switchDirection();
        directionSwitched = true;
    }

    private void maybeSwitchConversationDirectionOnRecv(Duplex duplex)
    {
        Duplex currentDuplex = conversationDirection.isReceive() ? Duplex.RECEIVE : Duplex.SEND;
        if(currentDuplex != duplex)
        {
            switchConversationDirection();
        }
    }

    private ConversationReturn<CasualBuffer> createConversationReturn(Request conversationReplyMessage)
    {
        ErrorState maybeErrorState = conversationReplyMessage.getResultCode() == RESULT_CODE_UNKNOWN ? null : ErrorState.unmarshal(conversationReplyMessage.getResultCode());
        return ConversationReturn.of(conversationReplyMessage.getServiceBuffer(), maybeErrorState, conversationReplyMessage.getUserCode(), conversationReplyMessage.getDuplex());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CasualConversationImpl that = (CasualConversationImpl) o;
        return directionSwitched == that.directionSwitched && Objects.equals(managedConnection, that.managedConnection) && Objects.equals(corrId, that.corrId) && conversationDirection == that.conversationDirection && Objects.equals(execution, that.execution);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(managedConnection, corrId, conversationDirection, directionSwitched, execution);
    }

    @Override
    public String toString()
    {
        return "CasualConversationImpl{" +
                "managedConnection=" + managedConnection +
                ", corrId=" + corrId +
                ", conversationDirection=" + conversationDirection +
                ", directionSwitched=" + directionSwitched +
                ", execution=" + execution +
                '}';
    }

    @Override
    public void close()
    {
        managedConnection.getNetworkConnection().getConversationClose().close(corrId);
    }
}
