package se.kodarkatten.casual.jca.inbound.handler.buffer;

import se.kodarkatten.casual.api.buffer.CasualBuffer;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Pass Through Buffer Handler to facilitate dispatch
 * of a service call with an unknown buffer type.
 */
public class PassThroughBufferHandler implements BufferHandler
{
    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return true;
    }

    @Override
    public ServiceCallInfo fromBuffer(Proxy p, Method method, CasualBuffer buffer)
    {
        return ServiceCallInfo.of( method, new Object[]{buffer} );
    }

    @Override
    public CasualBuffer toBuffer(Object result)
    {
        return (CasualBuffer)result;
    }
}
