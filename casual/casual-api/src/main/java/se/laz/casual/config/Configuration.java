/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Configuration
{
    public static final String USE_EPOLL_ENV_VAR_NAME = "CASUAL_USE_EPOLL";
    public static final String UNMANAGED_ENV_VAR_NAME = "CASUAL_UNMANAGED";
    private final Inbound inbound;
    private final Domain domain;
    private final Outbound outbound;
    private final List<ReverseInbound> reverseInbound;
    private final Boolean useEpoll;
    private final Boolean unmanaged;
    public Configuration(Domain domain, Inbound inbound, Outbound outbound, List<ReverseInbound> reverseInbound, Boolean useEpoll, Boolean unmanaged)
    {
        this.domain = domain;
        this.inbound = inbound;
        this.outbound = outbound;
        this.reverseInbound = reverseInbound;
        this.useEpoll = useEpoll;
        this.unmanaged = unmanaged;
    }

    public Domain getDomain()
    {
        return domain == null ? Domain.of(null) : domain;
    }

    public Inbound getInbound()
    {
        return inbound == null ? Inbound.newBuilder().build() : inbound;
    }

    public Outbound getOutbound()
    {
        return outbound == null ? Outbound.newBuilder().build() : outbound;
    }

    public List<ReverseInbound> getReverseInbound()
    {
        return null == reverseInbound ? Collections.emptyList() : Collections.unmodifiableList(reverseInbound);
    }

    public Optional<Boolean> getUseEpoll()
    {
        return null == useEpoll ? getUseEPollFromEnv() : Optional.of(useEpoll);
    }

    public Optional<Boolean> getUnmanaged()
    {
        return null == unmanaged ? getUnmanagedFromEnv() : Optional.of(unmanaged);
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
        Configuration that = (Configuration) o;
        return Objects.equals(getInbound(), that.getInbound()) &&
                Objects.equals(getDomain(), that.getDomain()) &&
                Objects.equals(getOutbound(), that.getOutbound()) &&
                Objects.equals(getReverseInbound(), that.getReverseInbound()) &&
                Objects.equals(getUseEpoll(), that.getUseEpoll()) &&
                Objects.equals(getUnmanaged(), that.getUnmanaged());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getInbound(), getDomain(), getOutbound(), getReverseInbound(), getUseEpoll(), getUnmanaged());
    }

    @Override
    public String toString()
    {
        return "Configuration{" +
                "inbound=" + getInbound() +
                ", domain=" + getDomain() +
                ", outbound=" + getOutbound() +
                ", reverseInbound=" + getReverseInbound() +
                ", useEpoll=" + getUseEpoll().orElse(null) +
                ", unmanaged=" + getUnmanaged().orElse(null) +
                '}';
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private Inbound inbound;
        private Domain domain;
        private Outbound outbound;
        private List<ReverseInbound> reverseInbound = new ArrayList<>();
        private Boolean useEpoll;
        private Boolean unmanaged;

        private Builder()
        {
        }

        public Builder withInbound( Inbound inbound )
        {
            this.inbound = inbound;
            return this;
        }

        public Builder withDomain(Domain domain)
        {
            this.domain = domain;
            return this;
        }

        public Builder withOutbound( Outbound outbound )
        {
            this.outbound = outbound;
            return this;
        }

        public Builder withReverseInbound( ReverseInbound reverseInbound )
        {
            this.reverseInbound.add(reverseInbound);
            return this;
        }

        public Builder withUseEpoll( boolean useEpoll )
        {
            this.useEpoll = useEpoll;
            return this;
        }

        public Builder withUnmanaged( boolean unmanaged )
        {
            this.unmanaged = unmanaged;
            return this;
        }

        public Configuration build()
        {
            return new Configuration(domain, inbound, outbound, reverseInbound, useEpoll, unmanaged );
        }
    }

    private Optional<Boolean> getUseEPollFromEnv()
    {
        return getEnvValue(USE_EPOLL_ENV_VAR_NAME);
    }

    private Optional<Boolean> getUnmanagedFromEnv()
    {
        return getEnvValue(UNMANAGED_ENV_VAR_NAME);
    }

    private Optional<Boolean> getEnvValue(String envVarName)
    {
        String envString = System.getenv(envVarName);
        Boolean envValue = null == envString ? null : Boolean.parseBoolean(envString);
        return Optional.ofNullable(envValue);
    }

}
