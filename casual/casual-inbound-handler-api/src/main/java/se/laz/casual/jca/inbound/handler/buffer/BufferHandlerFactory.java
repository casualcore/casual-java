/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.spi.Prioritise;
import se.laz.casual.spi.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI factory for retrieving registered {@link BufferHandler}s.
 */
public final class BufferHandlerFactory
{
    private static final BufferHandler PASSTHROUGH_HANDLER = new PassThroughBufferHandler();
    private static final Map<String,BufferHandler> bufferHandlerCache = new ConcurrentHashMap<>();

    private BufferHandlerFactory()
    {
    }

    /**
     * Get all registered {@link BufferHandler} instances.
     *
     * @return available handlers.
     */
    public static List<BufferHandler> getHandlers()
    {
        List<BufferHandler> handlers = new ArrayList<>();
        for( BufferHandler h: ServiceLoader.load( BufferHandler.class ) )
        {
            handlers.add( h );
        }
        return handlers;
    }

    /**
     * Retrieve the most appropriate {@link BufferHandler} base on it's {@link Priority}.
     *
     * If there is no registered handler a PassThoughHandler is returned, which will ensure no transformation takes place.
     *
     * @param bufferType to get a handler for.
     * @return the buffer handler.
     */
    public static BufferHandler getHandler(String bufferType )
    {
        if( bufferHandlerCache.containsKey( bufferType ) )
        {
            return bufferHandlerCache.get( bufferType );
        }

        List<BufferHandler> handlers = getHandlers();
        Prioritise.highestToLowest( handlers );

        for( BufferHandler h: handlers )
        {
            if( h.canHandleBuffer( bufferType ) )
            {
                bufferHandlerCache.put( bufferType, h );
                return h;
            }
        }
        bufferHandlerCache.put( bufferType, PASSTHROUGH_HANDLER );
        return PASSTHROUGH_HANDLER;
    }
}
