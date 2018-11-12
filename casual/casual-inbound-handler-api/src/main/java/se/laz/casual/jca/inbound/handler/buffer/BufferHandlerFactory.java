/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.jca.inbound.handler.Prioritise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class BufferHandlerFactory
{
    private static final BufferHandler PASSTHROUGH_HANDLER = new PassThroughBufferHandler();

    private static ServiceLoader<BufferHandler> handlerLoader = ServiceLoader.load( BufferHandler.class );

    private static final Map<String,BufferHandler> bufferHandlerCache = new ConcurrentHashMap<>();

    private BufferHandlerFactory()
    {
    }

    public static List<BufferHandler> getHandlers()
    {
        List<BufferHandler> handlers = new ArrayList<>();
        for( BufferHandler h: handlerLoader )
        {
            handlers.add( h );
        }
        return handlers;
    }

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
