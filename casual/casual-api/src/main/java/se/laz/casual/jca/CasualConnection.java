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

}
