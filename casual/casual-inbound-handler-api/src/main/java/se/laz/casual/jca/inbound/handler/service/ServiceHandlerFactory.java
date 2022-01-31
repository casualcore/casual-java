/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service;

import se.laz.casual.spi.Prioritise;
import se.laz.casual.spi.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * SPI factory for retrieving registered {@link ServiceHandler} instances.
 */
public final class ServiceHandlerFactory
{
    private static final Logger LOG = Logger.getLogger(ServiceHandlerFactory.class.getName());
    private static final Map<String,ServiceHandler> serviceHandlerCache = new ConcurrentHashMap<>();

    private ServiceHandlerFactory()
    {}

    /**
     * Retrieve all registered {@link ServiceHandler}s available.
     *
     * @return available handers.
     */
    public static List<ServiceHandler> getHandlers()
    {
        List<ServiceHandler> handlers = new ArrayList<>();
        for( ServiceHandler h: ServiceLoader.load( ServiceHandler.class ) )
        {
            handlers.add( h );
        }
        return handlers;
    }

    /**
     * Retrieve the most appropriate {@link ServiceHandler} base on it's {@link Priority}.
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

        log(handlers);

        for( ServiceHandler h: handlers )
        {
            if( h.canHandleService( serviceName ) )
            {
                serviceHandlerCache.put( serviceName, h );
                LOG.info(() -> "service handler: " + h + " chosen for service: " + serviceName);
                return h;
            }
        }
        throw new ServiceHandlerNotFoundException( "None of the registered handlers, handle service named: " + serviceName );
    }

    private static void log(List<ServiceHandler> handlers)
    {
        LOG.info(()-> "# of service handlers: " + handlers.size() + "\n" + logHandlers(handlers));
    }

    private static String logHandlers(List<ServiceHandler> handlers)
    {
        StringBuilder sb = new StringBuilder("service handlers in priority order descending:");
        for(ServiceHandler handler : handlers)
        {
            sb.append("Handler: " + handler);
            sb.append("Priority: " + handler.getPriority());
        }
        return sb.toString();
    }
}
