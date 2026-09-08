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
     * @throws UnsupportedOperationException if network pooling is not configured
     */
    boolean isDomainDisconnecting();

    /**
     * Returns whether a connection to the specified remote domain reports shutdown.
     *
     * <p>You can call this method concurrently. It inspects existing connections
     * without allocating a connection or changing reference counts. The result
     * can change immediately after the method returns.
     *
     * <p>A missing pool or a domain without matching connections returns
     * {@code false}. This result does not establish that the domain is available.
     *
     * @param domainId the remote domain to inspect
     * @return {@code true} if any matching connection reports domain shutdown
     * @throws NullPointerException if {@code domainId} is {@code null}
     * @throws UnsupportedOperationException if network pooling is not configured
     */
    boolean isDomainDisconnecting(DomainId domainId);

    boolean isReverse();
    List<DomainId> getDomainIds();

}
