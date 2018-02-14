package se.kodarkatten.casual.jca.inbound.handler.test;

import se.kodarkatten.casual.api.service.ServiceInfo;
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest;
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse;
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandler;

public class TestHandler2 implements ServiceHandler
{
    public static final String SERVICE_2 = "testService2";

    @Override
    public boolean canHandleService(String serviceName)
    {
        if( serviceName.equals( SERVICE_2 ) )
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


}
