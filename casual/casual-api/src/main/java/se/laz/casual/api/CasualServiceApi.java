/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.service.ServiceDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
     * @param serviceName the name of the service to call.
     * @param data the data to send to the given service.
     * @param flags the atmi flags to send to the given service.
     * @throws se.laz.casual.network.connection.CasualConnectionException when connection errors occur.
     * @return result of the service call, which can be a failure result.
     */
    ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags);

    /**
     * Perform a synchonous call Casual Server.
     *
     * @param serviceName the name of the service to call.
     * @param data the data to send to the given service.
     * @param flags the atmi flags to send to the given service.
     * @param execution the execution to use
     * @throws se.laz.casual.network.connection.CasualConnectionException when connection errors occur.
     * @return result of the service call, which can be a failure result.
     */
    ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags, UUID execution);

    /**
     * Async call
     * Wraps up a call to tpacall in a worker thread
     *
     * Be aware to handle any exception when using get/join
     *
     * Note 1, if invoking tpacall with TPNOREPLY | TPNOTRAN - the result will be Optional.empty
     * as a reply is not expected nor in any way handled.
     *
     * Note 2, TPNOREPLY is only allowed in conjunction with TPNOTRAN
     *
     * @param serviceName the name of the service to call.
     * @param data the data to send to the given service.
     * @param flags the atmi flags to send to the given service.
     * @return The future from which the result can be obtained
     */
    CompletableFuture<Optional<ServiceReturn<CasualBuffer>>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags);

    /**
     * Async call
     * Wraps up a call to tpacall in a worker thread
     *
     * Be aware to handle any exception when using get/join
     *
     * Note 1, if invoking tpacall with TPNOREPLY | TPNOTRAN - the result will be Optional.empty
     * as a reply is not expected nor in any way handled.
     *
     * Note 2, TPNOREPLY is only allowed in conjunction with TPNOTRAN
     *
     * @param serviceName the name of the service to call.
     * @param data the data to send to the given service.
     * @param flags the atmi flags to send to the given service.
     * @param execution the execution to use
     * @return The future from which the result can be obtained
     */
    CompletableFuture<Optional<ServiceReturn<CasualBuffer>>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags, UUID execution);

    /**
     * Lookup if service exists
     * @param serviceName the service name
     * @throws se.laz.casual.network.connection.CasualConnectionException when connection errors occur.
     * @return true if service exists
     */
    boolean serviceExists( String serviceName);

    /**
     * Fetch detailed lookup data for a service
     * @param serviceName the service name
     * @return A list of every instance that handles the service, with metadata for each. An empty list if the service isn't found.
     */
    List<ServiceDetails> serviceDetails(String serviceName);
}