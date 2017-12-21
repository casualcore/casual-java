package se.kodarkatten.casual.jca.inbound.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class CasualHandlerFactory
{
    private static ServiceLoader<CasualHandler> handlerLoader = ServiceLoader.load( CasualHandler.class );

    private static final Map<String,CasualHandler> serviceHandlerCache = new ConcurrentHashMap<>();

    private CasualHandlerFactory()
    {

    }

    public static List<CasualHandler> getHandlers()
    {
        List<CasualHandler> handlers = new ArrayList<>();
        for( CasualHandler h: handlerLoader )
        {
            handlers.add( h );
        }
        return handlers;
    }

    public static CasualHandler getHandler(String serviceName )
    {
        if( serviceHandlerCache.containsKey( serviceName ) )
        {
            return serviceHandlerCache.get( serviceName );
        }

        for( CasualHandler h: getHandlers() )
        {
            if( h.canHandleService( serviceName ) )
            {
                serviceHandlerCache.put( serviceName, h );
                return h;
            }
        }
        throw new CasualHandlerException( "None of the registered handlers, handle service named: " + serviceName );
    }
}
