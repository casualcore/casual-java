/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;

import java.io.Serializable;
import java.util.List;

/**
 * CasualConnectionFactory
 *
 * @version $Revision: $
 */
public interface CasualConnectionFactory extends Serializable, Referenceable
{
   /**
    * Get connection from factory
    *
    * @return DefaultNetworkConnection instance
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   CasualConnection getConnection() throws ResourceException;

    /**
     * Get connection from factory
     *
     * @param connectionRequestInfo connection specific data.
     * @return DefaultNetworkConnection instance
     * @exception ResourceException Thrown if a connection can't be obtained
     */
    CasualConnection getConnection(ConnectionRequestInfo connectionRequestInfo) throws ResourceException;

    /**
     * Returns whether a connection to this factory's remote domain reports shutdown.
     *
     * <p>You can call this method concurrently. It inspects existing connections
     * without allocating a connection or changing reference counts. The result
     * can change immediately after the method returns.
     *
     * <p>A missing or empty pool returns {@code false}. This result does not
     * establish that the remote domain is available.
     *
     * @return {@code true} if any connection in the pool reports domain shutdown
     * @throws IllegalStateException if the existing pool is an unpinned reverse pool
     */
    boolean isDomainDisconnecting();

    /**
     * Returns whether a connection to the specified remote domain in a reverse pool reports shutdown.
     *
     * <p>You can call this method concurrently. It inspects existing connections
     * without allocating a connection or changing reference counts. The result
     * can change immediately after the method returns.
     *
     * <p>You must use a registered reverse pool. A domain without matching connections
     * returns {@code false}. This result does not establish that the domain is available.
     *
     * @param domainId the remote domain to inspect
     * @return {@code true} if any matching connection reports domain shutdown
     * @throws NullPointerException if {@code domainId} is {@code null}
     * @throws IllegalStateException if the pool is missing or is not a reverse pool
     */
    boolean isDomainDisconnecting(DomainId domainId);

    /**
     * Returns whether this connection factory uses a reverse outbound pool.
     *
     * @return {@code true} if this factory uses a reverse outbound pool;
     *         otherwise, {@code false}
     */
    boolean isReverse();

    /**
     * Returns the distinct remote domain IDs currently represented in this pool.
     *
     * <p>A standard outbound pool returns at most one domain ID. A reverse outbound
     * pool returns one domain ID for each connected EIS. Either pool can return an
     * empty list when it has no established connections.
     *
     * <p>The returned list is an unmodifiable snapshot. Connections can change
     * immediately after this method returns.
     *
     * @return the distinct domain IDs currently represented in the pool
     */
    List<DomainId> getDomainIds();
}
