/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
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
    private Queue(String name, ProtocolVersion protocolVersion)
    {
        this.name = name;
        this.protocolVersion = protocolVersion;
    }

    public static Queue of(String name, ProtocolVersion protocolVersion)
    {
        Objects.requireNonNull(name, "name can not be null");
        Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
        return new Queue(name, protocolVersion);
    }

    public String getName()
    {
        return name;
    }

    public long getRetries()
    {
        return retries;
    }

    public Queue setRetries(long retries)
    {
        this.retries = retries;
        return this;
    }

    public long getRetryDelay()
    {
        if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            throw new CasualProtocolException("retry delay not available in protocol version " + protocolVersion);
        }
        return retryDelay;
    }

    public Queue setRetryDelay(long retryDelay)
    {
        this.retryDelay = retryDelay;
        return this;
    }

    public boolean isEnqueueEnabled()
    {
        if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            throw new CasualProtocolException("enqueue enabled is not available in protocol version " + protocolVersion);
        }
        return enqueueEnabled;
    }

    public Queue setEnqueueEnabled(boolean enqueueEnabled)
    {
        this.enqueueEnabled = enqueueEnabled;
        return this;
    }

    public boolean isDequeueEnabled()
    {
        if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            throw new CasualProtocolException("dequeue enabled is not available in protocol version " + protocolVersion);
        }
        return dequeueEnabled;
    }

    public Queue setDequeueEnabled(boolean dequeueEnabled)
    {
        this.dequeueEnabled = dequeueEnabled;
        return this;
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
        final long networkSize = ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion)
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
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
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
                dequeueEnabled == queue.dequeueEnabled && Objects.equals(getName(), queue.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getName(), getRetries(), getRetryDelay(), isEnqueueEnabled(), isDequeueEnabled());
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
}
