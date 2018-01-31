package se.kodarkatten.casual.jca.inbound.handler.buffer;

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

        for( BufferHandler h: getHandlers() )
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
