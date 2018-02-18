package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.CasualQueueApi;
import se.kodarkatten.casual.api.CasualServiceApi;

/**
 * NetworkConnection handle used in the application to call Casual Services.
 *
 * @version $Revision: $
 */
public interface CasualConnection extends CasualServiceApi, CasualQueueApi, AutoCloseable
{
    /**
     * Clean up the connection handle and close.
     */
    @Override
    void close();
}
