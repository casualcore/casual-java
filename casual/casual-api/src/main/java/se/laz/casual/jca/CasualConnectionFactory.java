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
     * Returns whether an existing pooled connection reports remote domain shutdown.
     *
     * <p>You can call this method concurrently. It does not allocate a connection.
     * A missing or empty pool returns {@code false}; this does not establish availability.
     * The result can change immediately after this method returns.
     *
     * @return whether any connection reports remote domain shutdown
     * @throws UnsupportedOperationException if network pooling is not configured
     */
    boolean isDomainDisconnecting();
}
