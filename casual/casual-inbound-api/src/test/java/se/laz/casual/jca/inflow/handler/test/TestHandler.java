/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow.handler.test;

import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;

import java.util.List;

public class TestHandler implements ServiceHandler
{
    public static final String SERVICE_1 = "testService1";
    public static final String SERVICE_THROWS = "testServiceThrows";

    private static final List<String> HANDLED_SERVICES = List.of(SERVICE_1, SERVICE_THROWS);
    private static final List<String> AVAILABLE_SERVICES = List.of(SERVICE_THROWS);

    @Override
    public boolean canHandleService(String serviceName)
    {
        return HANDLED_SERVICES.contains(serviceName);
    }

    @Override
    public boolean isServiceAvailable(String serviceName)
    {
        return AVAILABLE_SERVICES.contains(serviceName);
    }

    @Override
    public InboundResponse invokeService(InboundRequest request)
    {
        if (SERVICE_THROWS.equals(request.getServiceName()))
        {
            throw new RuntimeException("this service throws");
        }

        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(String serviceName)
    {
        return null;
    }


}
