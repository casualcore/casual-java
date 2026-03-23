/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain;

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.messages.parseinfo.DiscoveryReplySizes;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class Queue
{
    private final String name;
    private final ProtocolVersion protocolVersion;
    private long retries;
    // these are only available in protocol version >= 1.4
    private long retryDelay;
    private boolean enqueueEnabled;
    private boolean dequeueEnabled;
    private Queue(Builder builder)
    {
        name = builder.name;
        protocolVersion = builder.protocolVersion;
        retries = builder.retries;
        retryDelay = builder.retryDelay;
        enqueueEnabled = builder.enqueueEnabled;
        dequeueEnabled = builder.dequeueEnabled;
    }

    public String getName()
    {
        return name;
    }

    public long getRetries()
    {
        return retries;
    }

    public long getRetryDelay()
    {
        if(!ProtocolVersion.isGreaterOrEqualToOneFour(protocolVersion))
        {
            throw new CasualProtocolException("retry delay not available in protocol version " + protocolVersion);
        }
        return retryDelay;
    }

    public boolean isEnqueueEnabled()
    {
        if(!ProtocolVersion.isGreaterOrEqualToOneFour(protocolVersion))
        {
            throw new CasualProtocolException("enqueue enabled is not available in protocol version " + protocolVersion);
        }
        return enqueueEnabled;
    }

    public boolean isDequeueEnabled()
    {
        if(!ProtocolVersion.isGreaterOrEqualToOneFour(protocolVersion))
        {
            throw new CasualProtocolException("dequeue enabled is not available in protocol version " + protocolVersion);
        }
        return dequeueEnabled;
    }

    /**
     * We assume that everything for a Queue fits into Integer.MAX_VALUE
     * If not, we throw a CasualProtocolException
     * @return the network bytes.
     */
    public List<byte[]> toNetworkBytes()
    {
        final List<byte[]> l = new ArrayList<>();
        final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        final long networkSize = ProtocolVersion.isGreaterOrEqualToOneFour(protocolVersion)
                ? DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize() + nameBytes.length + (long)DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize()
                + DiscoveryReplySizes.QUEUES_ELEMENT_RETRY_DELAY.getNetworkSize() + DiscoveryReplySizes.QUEUES_ELEMENT_ENQUEUE_ENABLED.getNetworkSize()
                + DiscoveryReplySizes.QUEUES_ELEMENT_DEQUEUE_ENABLED.getNetworkSize()
                : DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize() + nameBytes.length + (long)DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize();
        if(networkSize > Integer.MAX_VALUE)
        {
            throw new CasualProtocolException("Queue byte size is larger than Integer.MAX_VALUE: " + networkSize);
        }
        final ByteBuffer b = ByteBuffer.allocate((int)networkSize);
        b.putLong(nameBytes.length)
         .put(nameBytes)
         .putLong(retries);
        if(ProtocolVersion.isGreaterOrEqualToOneFour(protocolVersion))
        {
            b.putLong(retryDelay)
             .put(((enqueueEnabled) ? (byte)(1) : (byte)(0)))
             .put(((dequeueEnabled) ? (byte)(1) : (byte)(0)));
        }
        l.add(b.array());
        return l;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Queue queue))
        {
            return false;
        }
        return retries == queue.retries && retryDelay == queue.retryDelay && enqueueEnabled == queue.enqueueEnabled &&
                dequeueEnabled == queue.dequeueEnabled && Objects.equals(getName(), queue.getName()) && protocolVersion == queue.protocolVersion;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, retries, retryDelay, enqueueEnabled, dequeueEnabled, protocolVersion);
    }

    @Override
    public String toString()
    {
        return new StringJoiner(", ", Queue.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("retries=" + retries)
                .add("retryDelay=" + retryDelay)
                .add("enqueueEnabled=" + enqueueEnabled)
                .add("dequeueEnabled=" + dequeueEnabled)
                .toString();
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private String name;
        private ProtocolVersion protocolVersion;
        private long retries;
        private long retryDelay;
        private boolean enqueueEnabled;
        private boolean dequeueEnabled;

        private Builder()
        {
        }

        public static Builder newBuilder()
        {
            return new Builder();
        }

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withProtocolVersion(ProtocolVersion protocolVersion)
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder withRetries(long retries)
        {
            this.retries = retries;
            return this;
        }

        public Builder withRetryDelay(long retryDelay)
        {
            this.retryDelay = retryDelay;
            return this;
        }

        public Builder withEnqueueEnabled(boolean enqueueEnabled)
        {
            this.enqueueEnabled = enqueueEnabled;
            return this;
        }

        public Builder withDequeueEnabled(boolean dequeueEnabled)
        {
            this.dequeueEnabled = dequeueEnabled;
            return this;
        }

        public Queue build()
        {
            Objects.requireNonNull(name, "name can not be null");
            Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
            return new Queue(this);
        }
    }
}
