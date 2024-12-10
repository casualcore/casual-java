/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.conversation;

import se.laz.casual.api.Conversation;
import se.laz.casual.api.flags.ErrorState;

import java.util.Objects;
import java.util.Optional;

/**
 *  The result from tpconnect
 *  On return it either contains:
 *  ErrorState.OK and a Conversation
 *  or
 *  !ErrorState.OK and no Conversation
 *
 *  Always use this with try with resources.
 *  If you do not, any valid conversation will not be closed properly.
 */

public class TpConnectReturn implements AutoCloseable
{
    private final Conversation conversation;
    private final ErrorState errorState;

    private TpConnectReturn(Conversation conversation, ErrorState errorState)
    {
        this.conversation = conversation;
        this.errorState = errorState;
    }

    public static TpConnectReturn of(Conversation conversation)
    {
        Objects.requireNonNull(conversation, "conversation can not be null");
        return new TpConnectReturn(conversation, ErrorState.OK);
    }

    public static TpConnectReturn of(ErrorState errorState)
    {
        Objects.requireNonNull(errorState, "errorState can not be null");
        return new TpConnectReturn(null, errorState);
    }

    public Optional<Conversation> getConversation()
    {
        return Optional.ofNullable(conversation);
    }

    public ErrorState getErrorState()
    {
        return errorState;
    }

    @Override
    public void close()
    {
        getConversation().ifPresent(Conversation::close);
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
        TpConnectReturn that = (TpConnectReturn) o;
        return Objects.equals(getConversation(), that.getConversation()) && getErrorState() == that.getErrorState();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getConversation(), getErrorState());
    }

    @Override
    public String toString()
    {
        return "TpConnectReturn{" +
                "conversation=" + conversation +
                ", errorState=" + errorState +
                '}';
    }
}
