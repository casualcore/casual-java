/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue;

import se.laz.casual.api.flags.ErrorState;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class EnqueueReturn
{
    private final UUID id;
    private final ErrorState errorState;
    private final QueueErrorCode errorCode;

    private EnqueueReturn(UUID id, ErrorState errorState, QueueErrorCode errorCode)
    {
        Objects.requireNonNull(errorState, "errorState can't be null");
        this.id = id;
        this.errorState = errorState;
        this.errorCode = errorCode;
    }

    public Optional<UUID> getId()
    {
        return Optional.ofNullable(id);
    }

    public ErrorState getErrorState()
    {
        return errorState;
    }

    /**
     * Only available when using gw protocol version >= 1.3
     * @return the error code, if available
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
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        EnqueueReturn enqueueReturn = (EnqueueReturn) o;

        return Objects.equals(id, enqueueReturn.id) && errorState.equals(enqueueReturn.getErrorState());
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hashCode(id);
        result = 31 * result + errorState.hashCode();
        result = 31 * result + Objects.hashCode(errorCode);
        return result;
    }

    @Override
    public String toString()
    {
        return "EnqueueReturn{" +
                "id=" + id +
                ", errorState=" + errorState +
                ", errorCode=" + errorCode +
                '}';
    }

    public static final class Builder
    {
        private UUID id;
        private ErrorState errorState;
        private QueueErrorCode errorCode;

        public Builder withId(UUID id)
        {
            this.id = id;
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

        public EnqueueReturn build()
        {
            return new EnqueueReturn(id, errorState, errorCode);
        }
    }
}
