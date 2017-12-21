package se.kodarkatten.casual.jca.inbound.handler.test;

import se.kodarkatten.casual.jca.inbound.handler.CasualHandler;
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest;
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse;

public class TestHandler2 implements CasualHandler
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


}
