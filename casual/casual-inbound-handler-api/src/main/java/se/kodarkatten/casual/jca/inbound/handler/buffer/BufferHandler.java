package se.kodarkatten.casual.jca.inbound.handler.buffer;

import se.kodarkatten.casual.api.buffer.CasualBuffer;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public interface BufferHandler
{
    boolean canHandleBuffer( String bufferType );

    ServiceCallInfo fromBuffer(Proxy p, Method method, CasualBuffer buffer );

    CasualBuffer toBuffer( Object result );
}
