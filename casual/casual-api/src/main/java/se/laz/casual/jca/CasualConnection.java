/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.CasualConversationApi;
import se.laz.casual.api.CasualDiscoveryApi;
import se.laz.casual.api.CasualQueueApi;
import se.laz.casual.api.CasualServiceApi;

import java.util.List;

/**
 * NetworkConnection handle used in the application to call Casual Services.
 *
 * @version $Revision: $
 */
public interface CasualConnection extends CasualServiceApi, CasualQueueApi, CasualConversationApi, CasualDiscoveryApi, AutoCloseable
{
    /**
     * Clean up the connection handle and close.
     */
    @Override
    void close();

    /**
     * Add a connection observer
     * @param observer - a connection observer
     */
    void addConnectionObserver(ConnectionObserver observer);

    /**
     * Returns the domain id of the connected domain.
     * @return DomainId - the domain id of the connected domain
     */
    DomainId getDomainId();

    /**
     * Check if the connection is disconnecting
     * This means that the domain that we are connected to is going down
     * and is currently draining in flight transactions but the actual connection
     * is not yet gone
     * @return true if disconnecting, false if not
     */
    boolean isDomainDisconnecting();

    /**
     * Check if this connection is backed by a reverse pool
     * @return true if backed by a reverse pool, false if not
     */
    boolean isReversePool();

    /**
     * The domain ids currently backing the reverse pool - if it is a reverse pool.
     *
     * For a reverse pool these are the currently connected instances, one entry per instance no
     * matter how many connections each of them has established. A connection towards a specific
     * instance can then be obtained via
     * {@link CasualConnectionFactory#getConnection(jakarta.resource.spi.ConnectionRequestInfo)}
     * using a {@link CasualRequestInfo} with that domain id.
     * @return the domain ids currently backing this connections pool
     */
    List<DomainId> getPoolDomainIds();

}
