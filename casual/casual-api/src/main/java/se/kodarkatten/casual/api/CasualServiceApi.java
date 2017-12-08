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
     *
     * @return result of the service call, which can be a failure result.
     */
    ServiceReturn<CasualBuffer> tpcall( String serviceName, CasualBuffer data, Flag<AtmiFlags> flags);

    /**
     * Async call
     * Wraps up a call to tpcall in a worker thread
     * @param serviceName
     * @param data
     * @param flags
     * @return The future from which the result can be obtained
     */
    CompletableFuture<ServiceReturn<CasualBuffer>> tpacall( String serviceName, CasualBuffer data, Flag<AtmiFlags> flags);
}