/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;

import java.util.HashMap;
import java.util.Map;

public class TestServiceHandler implements ServiceHandler
{
    private static final Map<String, Boolean> services = new HashMap<>();

    public void registerService( String name )
    {
        services.put( name, false );
    }

    public void makeServiceAvailable( String name )
    {
        services.put( name, true );
    }

    public void clear( )
    {
        services.clear();
    }

    @Override
    public boolean canHandleService( String serviceName )
    {
        return services.containsKey( serviceName );
    }

    @Override
    public boolean isServiceAvailable( String serviceName )
    {
        return services.getOrDefault( serviceName, false );
    }

    @Override
    public InboundResponse invokeService( InboundRequest request )
    {
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo( String serviceName )
    {
        return null;
    }
}
