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
    /**
     * Volatile since we need to ensure visibility of the protocol version across threads ( Inbound, Reverse Inbound, Outbound)
     * For example Inbound:
     *  - The protocol version is written on one Netty thread (from the thread pool) when a DOMAIN_CONNECT_REQUEST message is received and handled by the message handler.
     *  - It is read from potentially a different Netty thread when decoding subsequent incoming messages in the Netty decoder.
     *  - Volatile ensures that the write is immediately visible to all threads holding a reference to this holder, preventing stale reads due to CPU caching / memory ordering.
     */

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
            // Either protocol version is set in the outbound network connection after the handshake has been carried out OR
            // for inbound, when decoding the domain connect request message, we resolve the protocol version there and set it
            // For reverse inbound, this works exactly the same way as for inbound.
            // Thus, this should NEVER EVER HAPPEN
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
