/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.config;

import java.util.Objects;

public final class EventServer
{
    private final int portNumber;
    private final boolean useEpoll;

    private EventServer(Builder builder)
    {
        portNumber = builder.portNumber;
        useEpoll = builder.useEpoll;
    }

    public int getPortNumber()
    {
        return portNumber;
    }

    public boolean isUseEpoll()
    {
        return useEpoll;
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
        EventServer that = (EventServer) o;
        return getPortNumber() == that.getPortNumber() && isUseEpoll() == that.isUseEpoll();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getPortNumber(), isUseEpoll());
    }

    @Override
    public String toString()
    {
        return "EventServer{" +
                "portNumber=" + portNumber +
                ", useEpoll=" + useEpoll +
                '}';
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private int portNumber = 7698;
        private boolean useEpoll;

        private Builder()
        {}

        public static Builder builder()
        {
            return new Builder();
        }

        public Builder withPortNumber(int portNumber)
        {
            this.portNumber = portNumber;
            return this;
        }

        public Builder withUseEpoll(boolean useEpoll)
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public EventServer build()
        {
            return new EventServer(this);
        }
    }
}
