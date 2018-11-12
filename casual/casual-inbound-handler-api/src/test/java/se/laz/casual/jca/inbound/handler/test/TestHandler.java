/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.Priority;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;

public class TestHandler implements ServiceHandler
{
    public static final String SERVICE_1 = "testService1";
    public static final String SERVICE_COMMON = "testCommonService";

    @Override
    public boolean canHandleService(String serviceName)
    {
        if( serviceName.equals( SERVICE_1 ) )
        {
            return true;
        }
        if( serviceName.equals( SERVICE_COMMON ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean isServiceAvailable(String serviceName)
    {
        return false;
    }

    @Override
    public InboundResponse invokeService(InboundRequest request)
    {
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(String serviceName)
    {
        return null;
    }

    @Override
    public Priority getPriority()
    {
        return Priority.LEVEL_9;
    }


}
