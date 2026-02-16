/*
 * Copyright (c) 2022 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue;

import se.laz.casual.api.flags.ErrorState;

import java.util.Objects;
import java.util.Optional;

public class DequeueReturn
{
    private final QueueMessage queueMessage;
    private final ErrorState errorState;
    // only available in protocol version >= 1.3
    private final QueueErrorCode errorCode;

    private DequeueReturn(QueueMessage queueMessage, ErrorState errorState, QueueErrorCode errorCode)
    {
        Objects.requireNonNull(errorState, "errorState can't be null");
        this.queueMessage = queueMessage;
        this.errorState = errorState;
        this.errorCode = errorCode;
    }

    public Optional<QueueMessage> getQueueMessage()
    {
        return Optional.ofNullable(queueMessage);
    }

    public ErrorState getErrorState()
    {
        return errorState;
    }

    /**
     * Only available when using protocol version >= 1.3
     * @return
     */
    public Optional<QueueErrorCode> getErrorCode()
    {
        return Optional.ofNullable(errorCode);
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof DequeueReturn that))
        {
            return false;
        }
        return Objects.equals(queueMessage, that.queueMessage) && errorState == that.errorState && errorCode == that.errorCode;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(queueMessage, errorState, errorCode);
    }

    @Override
    public String toString()
    {
        return "DequeueReturn{" + "queueMessage=" + queueMessage +
                ", errorState=" + errorState.name() + '(' + errorState.getValue() + ')' +
                "}";
    }

    public static final class Builder
    {
        private QueueMessage queueMessage;
        private ErrorState errorState;
        private QueueErrorCode errorCode;

        public Builder withQueueMessage(QueueMessage queueMessage)
        {
            this.queueMessage = queueMessage;
            return this;
        }

        public Builder withErrorState(ErrorState errorState)
        {
            this.errorState = errorState;
            return this;
        }

        public Builder withErrorCode(QueueErrorCode errorCode)
        {
            this.errorCode = errorCode;
            return this;
        }
        public DequeueReturn build()
        {
            return new DequeueReturn(queueMessage, errorState, errorCode);
        }
    }
}
