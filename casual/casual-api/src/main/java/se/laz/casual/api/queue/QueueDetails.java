/*
 * Copyright (c) 2022 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.queue;

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.ProtocolVersion;

import java.util.Objects;

public class QueueDetails
{
    private final String name;
    private final long retries;
    private final ProtocolVersion protocolVersion;
    // these are only available in protocol version >= 1.4
    private final long retryDelay;
    private final boolean enqueueEnabled;
    private final boolean dequeueEnabled;
    public QueueDetails(String name, long retries, ProtocolVersion protocolVersion, long retryDelay, boolean enqueueEnabled, boolean dequeueEnabled)
    {
        this.name = name;
        this.retries = retries;
        this.protocolVersion = protocolVersion;
        this.retryDelay = retryDelay;
        this.enqueueEnabled = enqueueEnabled;
        this.dequeueEnabled = dequeueEnabled;
    }

    public QueueDetails(String name, long retries, ProtocolVersion protocolVersion)
    {
        this(name, retries, protocolVersion, 0, false, false);
    }

    public static QueueDetails of(String name, long retries, ProtocolVersion protocolVersion)
    {
        Objects.requireNonNull(name, "name can not be null");
        Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            throw new CasualProtocolException("retryDelay, enqueueEnabled, dequeueEnabled should be provided for " + ProtocolVersion.VERSION_1_4);
        }
        return new QueueDetails(name, retries, protocolVersion);
    }

    public static QueueDetails of(String name, long retries, ProtocolVersion protocolVersion, long retryDelay, boolean enqueueEnabled, boolean dequeueEnabled)
    {
        Objects.requireNonNull(name, "name can not be null");
        Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
        if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            throw new CasualProtocolException("retryDelay, enqueueEnabled, dequeueEnabled are not available for protocol version " + protocolVersion);
        }
        return new QueueDetails(name, retries, protocolVersion, retryDelay, enqueueEnabled, dequeueEnabled);
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
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            return retryDelay;
        }
        throw new CasualProtocolException("retry delay not available for protocol version " + protocolVersion);
    }

    public boolean isEnqueueEnabled()
    {
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            return enqueueEnabled;
        }
        throw new CasualProtocolException("enqueue enabled not available for protocol version " + protocolVersion);
    }

    public boolean isDequeueEnabled()
    {
        if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
        {
            return dequeueEnabled;
        }
        throw new CasualProtocolException("dequeue enabled not available for protocol version " + protocolVersion);
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
        return retries == that.retries && retryDelay == that.retryDelay && enqueueEnabled == that.enqueueEnabled &&
                dequeueEnabled == that.dequeueEnabled && Objects.equals(getName(), that.getName()) && protocolVersion == that.protocolVersion;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, retries, protocolVersion, retryDelay, enqueueEnabled, dequeueEnabled);
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer("QueueDetails{");
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
}
