/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound;

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.network.ProtocolVersion;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProtocolVersionValueHolder implements Supplier<ProtocolVersion>, Consumer<ProtocolVersion>
{
    // The decoder or message handler writes the negotiated version on a Netty event-loop thread.
    // Connection and decoding work can read it from another thread. Volatile safely publishes
    // the version to every thread that holds this shared holder.
    private volatile ProtocolVersion protocolVersion;

    private ProtocolVersionValueHolder()
    {}

    public static ProtocolVersionValueHolder of()
    {
        return new ProtocolVersionValueHolder();
    }

    @Override
    public ProtocolVersion get()
    {
        if(null == protocolVersion)
        {
            // Outbound decoding stores the version from the domain-connect reply. Inbound and
            // reverse inbound handling store it from the domain-connect request. Any later read
            // without a value indicates that the connection handshake ordering is broken.
            throw new CasualProtocolException("fatal: protocol version not set - should never be used in a context where it has not already been set");
        }
        return protocolVersion;
    }

    @Override
    public void accept(ProtocolVersion protocolVersion)
    {
        Objects.requireNonNull(protocolVersion, "protocolVersion can not be null");
        if(this.protocolVersion != null)
        {
            throw new CasualProtocolException("protocol version already set");
        }
        this.protocolVersion = protocolVersion;
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
        ProtocolVersionValueHolder that = (ProtocolVersionValueHolder) o;
        return protocolVersion == that.protocolVersion;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(protocolVersion);
    }

    @Override
    public String toString()
    {
        return "ProtocolVersionValueHolder{" +
                "protocolVersion=" + protocolVersion +
                '}';
    }
}
