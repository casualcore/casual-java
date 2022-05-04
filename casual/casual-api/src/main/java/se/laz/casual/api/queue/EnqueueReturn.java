/*
 * Copyright (c) 2022, The casual project. All rights reserved.
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

    private EnqueueReturn(UUID id, ErrorState errorState)
    {
        Objects.requireNonNull(errorState, "errorState can't be null");
        this.id = id;
        this.errorState = errorState;
    }

    public Optional<UUID> getId()
    {
        return Optional.ofNullable(id);
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
        EnqueueReturn enqueueReturn = (EnqueueReturn) o;

        return Objects.equals(id, enqueueReturn.id) && errorState.equals(enqueueReturn.getErrorState());
    }

    @Override
    public int hashCode()
    {
        return id.hashCode() + Integer.hashCode(errorState.getValue());
    }

    @Override
    public String toString()
    {
        return "EnqueueReturn{" + "id=" + id +
                ", errorState=" + errorState.name() + '(' + errorState.getValue() + ')' +
                "}";
    }

    public static final class Builder
    {
        private UUID id;
        private ErrorState errorState;

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

        public EnqueueReturn build()
        {
            return new EnqueueReturn(id, errorState);
        }
    }
}
