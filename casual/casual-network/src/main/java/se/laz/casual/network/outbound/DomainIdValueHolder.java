/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.jca.DomainId;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Stores the peer domain ID established by an outbound domain-connect reply.
 *
 * <p>This class safely publishes the domain ID between the Netty event-loop thread and
 * connection users. You can set the value once and read it after the connect reply has
 * been decoded.
 */
public final class DomainIdValueHolder implements Supplier<DomainId>, Consumer<DomainId>
{
    private volatile DomainId domainId;

    private DomainIdValueHolder()
    {}

    /**
     * Creates an empty domain ID holder.
     *
     * @return an empty domain ID holder
     */
    public static DomainIdValueHolder of()
    {
        return new DomainIdValueHolder();
    }

    /**
     * Returns the connected peer's domain ID.
     *
     * @return the connected peer's domain ID
     * @throws CasualProtocolException if the domain-connect reply has not established the domain ID
     */
    @Override
    public DomainId get()
    {
        if(domainId == null)
        {
            throw new CasualProtocolException("fatal: domain id not set - should never be used in a context where it has not already been set");
        }
        return domainId;
    }

    /**
     * Stores the connected peer's domain ID.
     *
     * @param domainId the connected peer's domain ID
     * @throws NullPointerException if {@code domainId} is {@code null}
     * @throws CasualProtocolException if the domain ID has already been set
     */
    @Override
    public void accept(DomainId domainId)
    {
        Objects.requireNonNull(domainId, "domainId can not be null");
        if(this.domainId != null)
        {
            throw new CasualProtocolException("domain id already set");
        }
        this.domainId = domainId;
    }

    @Override
    public String toString()
    {
        return "DomainIdValueHolder{" +
                "domainId=" + domainId +
                '}';
    }
}
