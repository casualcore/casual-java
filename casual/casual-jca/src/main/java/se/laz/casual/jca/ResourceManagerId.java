/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Objects;

/**
 * Identifies an outbound Casual resource manager for XA branch correlation and pending-transaction tracking.
 *
 * <p>For a normal outbound connection, the resource manager identity is the configured target
 * {@link Address}. For a reverse outbound connection pinned to a connected reverse inbound peer,
 * the identity is the remote {@link DomainId}. An address-backed identity never equals a
 * domain-ID-backed identity.
 *
 * <p>This class is immutable and thread-safe.
 */
public final class ResourceManagerId
{
    private final Address address;
    private final DomainId domainId;

    private ResourceManagerId(Address address, DomainId domainId)
    {
        this.address = address;
        this.domainId = domainId;
    }

    /**
     * Creates a resource manager identifier backed by a normal outbound address.
     *
     * @param address the target outbound address
     * @return an address-backed resource manager identifier
     * @throws NullPointerException if {@code address} is {@code null}
     */
    public static ResourceManagerId of(Address address)
    {
        Objects.requireNonNull(address, "address must not be null");
        return new ResourceManagerId(address, null);
    }

    /**
     * Creates a resource manager identifier backed by a remote reverse inbound domain ID.
     *
     * @param domainId the remote domain ID
     * @return a domain-ID-backed resource manager identifier
     * @throws NullPointerException if {@code domainId} is {@code null}
     */
    public static ResourceManagerId of(DomainId domainId)
    {
        Objects.requireNonNull(domainId, "domainId must not be null");
        return new ResourceManagerId(null, domainId);
    }

    /**
     * Resolves the resource manager identifier for the given managed connection.
     *
     * <p>When the managed connection is pinned to a remote domain (reverse outbound), this method
     * uses the pinned {@link DomainId}. Otherwise, it uses the managed connection's target
     * {@link Address}.
     *
     * @param connection the managed connection
     * @return the resolved resource manager identifier
     * @throws NullPointerException if {@code connection} is {@code null}, or if an unpinned
     *         connection has no target address
     */
    public static ResourceManagerId of(CasualManagedConnection connection)
    {
        Objects.requireNonNull(connection, "connection must not be null");
        return connection.getPinnedDomainId()
                .map(ResourceManagerId::of)
                .orElseGet(() -> of(connection.getAddress()));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ResourceManagerId that))
        {
            return false;
        }
        return Objects.equals(address, that.address) && Objects.equals(domainId, that.domainId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(address, domainId);
    }

    @Override
    public String toString()
    {
        return address != null
                ? "ResourceManagerId{address=" + address + '}'
                : "ResourceManagerId{domainId=" + domainId + '}';
    }
}
