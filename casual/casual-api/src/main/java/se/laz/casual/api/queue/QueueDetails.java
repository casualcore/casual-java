/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.queue;

import se.laz.casual.network.ProtocolVersion;

import java.util.Objects;
import java.util.Optional;

public class QueueDetails
{
    private final String name;
    private final long retries;
    private final ProtocolVersion protocolVersion;
    private final Long retryDelay;
    private final Boolean enqueueEnabled;
    private final Boolean dequeueEnabled;

    private QueueDetails(Builder builder)
    {
        this.name = builder.name;
        this.retries = builder.retries;
        this.protocolVersion = builder.protocolVersion;
        this.retryDelay = builder.retryDelay;
        this.enqueueEnabled = builder.enqueueEnabled;
        this.dequeueEnabled = builder.dequeueEnabled;
    }

    public String getName()
    {
        return name;
    }

    public long getRetries()
    {
        return retries;
    }

    /**
     * @since protocol version 1.4
     * @return the retry delay, if available
     */
    public Optional<Long> getRetryDelay()
    {
        return Optional.ofNullable(retryDelay);
    }

    /**
     * @since protocol version 1.4
     * @return whether enqueue is enabled, if available
     */
    public Optional<Boolean> isEnqueueEnabled()
    {
        return Optional.ofNullable(enqueueEnabled);
    }

    /**
     * @since protocol version 1.4
     * @return whether dequeue is enabled, if available
     */
    public Optional<Boolean> isDequeueEnabled()
    {
        return Optional.ofNullable(dequeueEnabled);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof QueueDetails that))
        {
            return false;
        }
        return retries == that.retries && Objects.equals(retryDelay, that.retryDelay) && Objects.equals(enqueueEnabled, that.enqueueEnabled) &&
                Objects.equals(dequeueEnabled, that.dequeueEnabled) && Objects.equals(getName(), that.getName()) && protocolVersion == that.protocolVersion;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, retries, protocolVersion, retryDelay, enqueueEnabled, dequeueEnabled);
    }

    @Override
    public String toString()
    {
        return "QueueDetails{" +
                "name='" + name + '\'' +
                ", retries=" + retries +
                ", protocolVersion=" + protocolVersion +
                ", retryDelay=" + retryDelay +
                ", enqueueEnabled=" + enqueueEnabled +
                ", dequeueEnabled=" + dequeueEnabled +
                '}';
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private String name;
        private long retries;
        private ProtocolVersion protocolVersion;
        private Long retryDelay;
        private Boolean enqueueEnabled;
        private Boolean dequeueEnabled;

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withRetries(long retries)
        {
            this.retries = retries;
            return this;
        }

        public Builder withProtocolVersion(ProtocolVersion protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder withRetryDelay(Long retryDelay)
        {
            this.retryDelay = retryDelay;
            return this;
        }

        public Builder withEnqueueEnabled(Boolean enqueueEnabled)
        {
            this.enqueueEnabled = enqueueEnabled;
            return this;
        }

        public Builder withDequeueEnabled(Boolean dequeueEnabled)
        {
            this.dequeueEnabled = dequeueEnabled;
            return this;
        }

        public QueueDetails build()
        {
            Objects.requireNonNull(name, "name can not be null");
            Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
            return new QueueDetails(this);
        }
    }
}
