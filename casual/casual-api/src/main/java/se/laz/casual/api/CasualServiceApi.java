/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;

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
    ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags);

    /**
     * Async call
     * Wraps up a call to tpcall in a worker thread
     * @param serviceName
     * @param data
     * @param flags
     * @return The future from which the result can be obtained
     */
    CompletableFuture<ServiceReturn<CasualBuffer>> tpacall( String serviceName, CasualBuffer data, Flag<AtmiFlags> flags);

    /**
     * Lookup if service exists
     * @param serviceName
     * @return true if service exists
     */
    boolean serviceExists( String serviceName);


}