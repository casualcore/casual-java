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
     * Add a connection observer
     * @param observer - a connection observer
     */
    void addConnectionObserver(ConnectionObserver observer);

    /**
     * Returns the domain id of the connected domain.
     * @return DomainId - the domain id of the connected domain
     */
    DomainId getDomainId();

}
