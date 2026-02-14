/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.queue;

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.ProtocolVersion;

import java.util.Objects;
import java.util.Optional;

public class QueueDetails
{
    private final String name;
    private final long retries;
    private final ProtocolVersion protocolVersion;
    // these are only available in protocol version >= 1.4
    private final Long retryDelay;
    private final Boolean enqueueEnabled;
    private final Boolean dequeueEnabled;
    private QueueDetails(String name, long retries, ProtocolVersion protocolVersion, Long retryDelay, Boolean enqueueEnabled, Boolean dequeueEnabled)
    {
        this.name = name;
        this.retries = retries;
        this.protocolVersion = protocolVersion;
        this.retryDelay = retryDelay;
        this.enqueueEnabled = enqueueEnabled;
        this.dequeueEnabled = dequeueEnabled;
    }

    private QueueDetails(Builder builder)
    {
        this(builder.name, builder.retries, builder.protocolVersion, builder.retryDelay, builder.enqueueEnabled, builder.dequeueEnabled);
    }

    public static QueueDetails of(String name, long retries, ProtocolVersion protocolVersion)
    {
        Objects.requireNonNull(name, "name can not be null");
        Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
        return QueueDetails.createBuilder()
                .withName(name)
                .withRetries(retries)
                .withProtocolVersion(protocolVersion)
                .build();
    }

    public String getName()
    {
        return name;
    }

    public long getRetries()
    {
        return retries;
    }

    public Optional<Long> getRetryDelay()
    {
        return Optional.ofNullable(retryDelay);
    }

    public Optional<Boolean> isEnqueueEnabled()
    {
        return Optional.ofNullable(enqueueEnabled);
    }

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
        final StringBuilder sb = new StringBuilder("QueueDetails{");
        sb.append("name='").append(name).append('\'');
        sb.append(", retries=").append(retries);
        sb.append(", protocolVersion=").append(protocolVersion);
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            sb.append(", retryDelay=").append(retryDelay);
            sb.append(", enqueueEnabled=").append(enqueueEnabled);
            sb.append(", dequeueEnabled=").append(dequeueEnabled);
        }
        sb.append('}');
        return sb.toString();
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
            if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion) &&
                    (null != retryDelay || null != enqueueEnabled || null != dequeueEnabled))
            {
                throw new CasualProtocolException("retryDelay, enqueueEnabled, dequeueEnabled are not available for protocol version " + protocolVersion);
            }
            if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion) &&
                    (null == retryDelay || null == enqueueEnabled || null == dequeueEnabled))
            {
                throw new CasualProtocolException("retryDelay, enqueueEnabled, dequeueEnabled should be provided for " + ProtocolVersion.VERSION_1_4);
            }
            return new QueueDetails(this);
        }
    }
}
