/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service;

import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.spi.Prioritisable;

public interface ServiceHandler extends Prioritisable
{
    /**
     * Determine if the handler know to handle a
     * service of this name.
     *
     * Differs from {@link #isServiceAvailable} in that a
     * service could be registered and possible to handle but unavailable,
     * in which case this method would return true,
     * but {@link #isServiceAvailable} would return false.
     *
     * @param serviceName name of the service.
     * @return if the service can be handled or not.
     */
    boolean canHandleService( String serviceName );

    /**
     * Determine if the provided service is available to receive requests
     * through this handler.
     *
     * Differs from {@link #canHandleService} in that a
     * service may not yet be available/ready to receive requests, but
     * this handler can handle the service when it is available.
     * In which case this method would return false,
     * but {@link #canHandleService} would return true.
     *
     * @param serviceName name
     * @return if the service is available.
     */
    boolean isServiceAvailable( String serviceName );

    /**
     * Invoke the service and return the result of this invokation.
     * @param request received from client.
     * @return response to return to client.
     */
    InboundResponse invokeService(InboundRequest request );

    /**
     * Returns the service object representation of the service
     * with this name.
     *
     * @param serviceName of the service to retrieve.
     * @return service object.
     */
    ServiceInfo getServiceInfo(String serviceName );
}
