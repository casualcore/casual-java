/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.CasualConversationAPI;
import se.laz.casual.api.CasualDiscoveryApi;
import se.laz.casual.api.CasualQueueApi;
import se.laz.casual.api.CasualServiceApi;

/**
 * NetworkConnection handle used in the application to call Casual Services.
 *
 * @version $Revision: $
 */
public interface CasualConnection extends CasualServiceApi, CasualQueueApi, CasualConversationAPI, CasualDiscoveryApi, AutoCloseable
{
    /**
     * Clean up the connection handle and close.
     */
    @Override
    void close();

    /**
     * Get the current domain id for this connection
     * @return the current domain id for this connection
     */
    DomainId getDomainID();

    /**
     * If a connection is gone, for whatever reason - it is just not usable anymore.
     *
     * When this happens, {@link ConnectionObserver#connectionGone(DomainId)} is called with the same domain id
     * that can be retrieved via {@link CasualConnection#getDomainID()}
     * @param observer - the observer that will be notified
     */
    void addConnectionObserver(ConnectionObserver observer);

}
