package se.kodarkatten.casual.jca.inbound.handler.service;

import se.kodarkatten.casual.jca.inbound.handler.HandlerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class ServiceHandlerFactory
{
    private static ServiceLoader<ServiceHandler> handlerLoader = ServiceLoader.load( ServiceHandler.class );

    private static final Map<String,ServiceHandler> serviceHandlerCache = new ConcurrentHashMap<>();

    private ServiceHandlerFactory()
    {

    }

    public static List<ServiceHandler> getHandlers()
    {
        List<ServiceHandler> handlers = new ArrayList<>();
        for( ServiceHandler h: handlerLoader )
        {
            handlers.add( h );
        }
        return handlers;
    }

    public static ServiceHandler getHandler(String serviceName )
    {
        if( serviceHandlerCache.containsKey( serviceName ) )
        {
            return serviceHandlerCache.get( serviceName );
        }

        for( ServiceHandler h: getHandlers() )
        {
            if( h.canHandleService( serviceName ) )
            {
                serviceHandlerCache.put( serviceName, h );
                return h;
            }
        }
        throw new HandlerException( "None of the registered handlers, handle service named: " + serviceName );
    }
}
