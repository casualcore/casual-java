/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service;

import se.laz.casual.jca.inbound.handler.Prioritise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI factory for retrieving registered {@link ServiceHandler} instances.
 */
public final class ServiceHandlerFactory
{
    private static ServiceLoader<ServiceHandler> handlerLoader = ServiceLoader.load( ServiceHandler.class );

    private static final Map<String,ServiceHandler> serviceHandlerCache = new ConcurrentHashMap<>();

    private ServiceHandlerFactory()
    {

    }

    /**
     * Retrieve all registered {@link ServiceHandler}s available.
     *
     * @return available handers.
     */
    public static List<ServiceHandler> getHandlers()
    {
        List<ServiceHandler> handlers = new ArrayList<>();
        for( ServiceHandler h: handlerLoader )
        {
            handlers.add( h );
        }
        return handlers;
    }

    /**
     * Retrieve the most appropriate {@link ServiceHandler} base on it's {@link se.laz.casual.jca.inbound.handler.Priority}.
     *
     * If there is no registered handler a {@link ServiceHandlerNotFoundException} is thrown.
     *
     * @param serviceName for which a handler is required.
     * @return the appropriate handler.
     */
    public static ServiceHandler getHandler(String serviceName )
    {
        if( serviceHandlerCache.containsKey( serviceName ) )
        {
            return serviceHandlerCache.get( serviceName );
        }

        List<ServiceHandler> handlers = getHandlers();
        Prioritise.highestToLowest( handlers );

        for( ServiceHandler h: handlers )
        {
            if( h.canHandleService( serviceName ) )
            {
                serviceHandlerCache.put( serviceName, h );
                return h;
            }
        }
        throw new ServiceHandlerNotFoundException( "None of the registered handlers, handle service named: " + serviceName );
    }
}
