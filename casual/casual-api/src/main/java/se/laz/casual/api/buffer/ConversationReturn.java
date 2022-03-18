/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer;

import se.laz.casual.api.conversation.Duplex;
import se.laz.casual.api.flags.ErrorState;

import java.util.Objects;
import java.util.Optional;

/**
 * Note that you should never yourself instantiate an instance of this class but it
 * is returned to you upon a recv via the {@link se.laz.casual.api.Conversation}
 */
public final class ConversationReturn<X extends CasualBuffer>
{
    private final X replyBuffer;

    private final ErrorState errorState;
    private final long userCode;
    private final Duplex duplex;

    private ConversationReturn(X replyBuffer, Optional<ErrorState> errorState, long userCode, Duplex duplex)
    {
        this.replyBuffer = replyBuffer;
        this.errorState = errorState.orElse(null);
        this.userCode = userCode;
        this.duplex = duplex;
    }

    public static <X extends CasualBuffer> ConversationReturn<X> of(X replyBuffer, Optional<ErrorState> errorState, long userCode, Duplex duplex)
    {
        Objects.requireNonNull(replyBuffer, "replyBuffer can not be null");
        Objects.requireNonNull(errorState, "errorState can not be null");
        Objects.requireNonNull(duplex, "duplex can not be null");
        return new ConversationReturn<>(replyBuffer, errorState, userCode, duplex);
    }

    /**
     * @return a subtype of CasualBuffer
     */
    public X getReplyBuffer()
    {
        return replyBuffer;
    }

    /**
     * ErrorState is optional
     * If it does not exist it means that it didn't make sense in that context
     * @return
     */
    public Optional<ErrorState> getErrorState()
    {
        return Optional.ofNullable(errorState);
    }

    public long getUserCode()
    {
        return userCode;
    }

    public Duplex getDuplex()
    {
        return duplex;
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
        ConversationReturn<?> that = (ConversationReturn<?>) o;
        return userCode == that.userCode && Objects.equals(replyBuffer, that.replyBuffer) && errorState == that.errorState && duplex == that.duplex;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(replyBuffer, errorState, userCode, duplex);
    }

    @Override
    public String toString()
    {
        return "ConversationReturn{" +
                "replyBuffer=" + replyBuffer +
                ", errorState=" + errorState +
                ", userCode=" + userCode +
                ", duplex=" + duplex +
                '}';
    }
}
