/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
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
     * service could be registered  but unavailable,
     * in which case this method would return true,
     * but {@link #isServiceAvailable} would return false.
     *
     * @param serviceName name of the service.
     * @return if the service can be handled or not.
     */
    boolean canHandleService( String serviceName );

    /**
     * Determine if the provided service is available
     * in this handler.
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
