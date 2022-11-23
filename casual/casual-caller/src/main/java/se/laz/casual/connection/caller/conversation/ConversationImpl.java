/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.conversation;

import se.laz.casual.api.Conversation;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ConversationReturn;
import se.laz.casual.jca.CasualConnection;

import java.util.Objects;

/**
 * Casual caller wrapper class for a conversation and the corresponding connection
 * This since the connection needs to be open in scope of the conversation.
 */
public class ConversationImpl implements Conversation
{
    private final CasualConnection connection;
    private final Conversation conversation;

    public ConversationImpl(CasualConnection connection, Conversation conversation)
    {
        this.conversation = conversation;
        this.connection = connection;
    }

    public static Conversation of(CasualConnection connection, Conversation conversation)
    {
        Objects.requireNonNull(connection, "connection can not be null");
        Objects.requireNonNull(conversation, "conversation can not be null");
        return new ConversationImpl(connection, conversation);
    }

    @Override
    public void tpdiscon()
    {
        conversation.tpdiscon();
    }

    @Override
    public ConversationReturn<CasualBuffer> tprecv()
    {
        return conversation.tprecv();
    }

    @Override
    public boolean isSending()
    {
        return conversation.isSending();
    }

    @Override
    public boolean isReceiving()
    {
        return conversation.isReceiving();
    }

    @Override
    public void tpsend(CasualBuffer data, boolean handOverControl)
    {
        conversation.tpsend(data, handOverControl);
    }

    @Override
    public void tpsend(CasualBuffer data, boolean handOverControl, long userCode)
    {
        conversation.tpsend(data, handOverControl, userCode);
    }

    @Override
    public boolean isDirectionSwitched()
    {
        return conversation.isDirectionSwitched();
    }

    @Override
    public void close()
    {
        conversation.close();
        connection.close();
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
        ConversationImpl that = (ConversationImpl) o;
        return Objects.equals(connection, that.connection) && Objects.equals(conversation, that.conversation);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(connection, conversation);
    }

    @Override
    public String toString()
    {
        return "ConversationImpl{" +
                "connection=" + connection +
                ", conversation=" + conversation +
                '}';
    }
}
