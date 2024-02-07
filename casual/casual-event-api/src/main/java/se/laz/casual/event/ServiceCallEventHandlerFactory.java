package se.laz.casual.event;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.spi.Prioritise;
import se.laz.casual.spi.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceCallEventHandlerFactory
{
    private static final List<ServiceCallEventHandler> serviceHandlerCache = getHandlers();

    private ServiceCallEventHandlerFactory()
    {}

    /**
     * Retrieve the most appropriate {@link ServiceCallEventHandler} based on it's {@link Priority}.
     *
     * If there is no registered handler a {@link CasualRuntimeException} is thrown.
     *
     * @return the appropriate handler.
     */
    public static ServiceCallEventHandler getHandler()
    {
        return serviceHandlerCache.stream()
                                  .findFirst()
                                  .orElseThrow(() -> new CasualRuntimeException("No ServiceCallEventHandler found"));
    }

    private static List<ServiceCallEventHandler> getHandlers()
    {
        List<ServiceCallEventHandler> handlers = new ArrayList<>();
        for( ServiceCallEventHandler h: ServiceLoader.load( ServiceCallEventHandler.class ) )
        {
            handlers.add( h );
        }
        Prioritise.highestToLowest(handlers);
        return handlers;
    }
}
