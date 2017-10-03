package se.kodarkatten.casual.api;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.Flag;

import java.util.concurrent.CompletableFuture;

/**
 * Casual Service API
 * providing methods to perform both synchronous
 * and asynchrous service calls to a Casual Server.
 *
 * @author jone
 */
public interface CasualServiceApi
{
    /**
     * Perform a synchonous call Casual Server.
     *
     * @param serviceName name of the service to call.
     * @param data to send to the given service.
     * @param flags to send to the given service.
     * @param bufferClass type of buffer being sent to the service.
     * @param <X>
     * @return result of the service call, which can be a failure result.
     */
    <X extends CasualBuffer> ServiceReturn<X> tpcall(String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass);


    <X extends CasualBuffer> CompletableFuture<ServiceReturn<X>> tpacall(String serviceName, X data, Flag<AtmiFlags> flags, Class<X> bufferClass);
}