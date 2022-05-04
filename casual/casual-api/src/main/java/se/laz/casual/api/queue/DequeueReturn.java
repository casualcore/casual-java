/*
 * Copyright (c) 2022, The casual project. All rights reserved.
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

    private DequeueReturn(QueueMessage queueMessage, ErrorState errorState)
    {
        Objects.requireNonNull(errorState, "errorState can't be null");
        this.queueMessage = queueMessage;
        this.errorState = errorState;
    }

    public Optional<QueueMessage> getQueueMessage()
    {
        return Optional.ofNullable(queueMessage);
    }

    public ErrorState getErrorState()
    {
        return errorState;
    }

    public static Builder createBuilder()
    {
        return new Builder();
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
        DequeueReturn dequeueReturn = (DequeueReturn) o;
        return Objects.equals(queueMessage, dequeueReturn.queueMessage) && errorState.equals(dequeueReturn.getErrorState());
    }

    @Override
    public int hashCode()
    {
        return queueMessage.hashCode() + Integer.hashCode(errorState.getValue());
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

        public DequeueReturn build()
        {
            return new DequeueReturn(queueMessage, errorState);
        }
    }
}
