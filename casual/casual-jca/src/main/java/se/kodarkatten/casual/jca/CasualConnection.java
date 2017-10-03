package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.CasualServiceApi;

/**
 * Connection handle used in the application to call Casual Services.
 *
 * @version $Revision: $
 */
public interface CasualConnection extends CasualServiceApi, AutoCloseable //ConversationApi and Queue API too probably
{
    /**
     * Clean up the connection handle and close.
     */
    void close();
}
