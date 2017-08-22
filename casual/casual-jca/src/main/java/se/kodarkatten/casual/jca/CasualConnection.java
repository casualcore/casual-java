package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.CasualServiceApi;
import se.kodarkatten.casual.api.services.CasualService;

/**
 * CasualConnection
 *
 * This is the interface of the Connection Proxy used in the application to Call Casual Services
 *
 * @version $Revision: $
 */
public interface CasualConnection extends CasualServiceApi, AutoCloseable //ConversationApi and Queue API too probably
{
    void close();
}
