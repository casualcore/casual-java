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

import java.util.List;

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
     * Get all known domain ids for this pool ( host:port)
     * @return list of know domain ids.
     */
    List<DomainId> getPoolDomainIds();

}
