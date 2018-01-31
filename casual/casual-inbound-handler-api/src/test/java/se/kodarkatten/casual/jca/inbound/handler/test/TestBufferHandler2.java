package se.kodarkatten.casual.jca.inbound.handler.test;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.jca.inbound.handler.buffer.BufferHandler;
import se.kodarkatten.casual.jca.inbound.handler.buffer.ServiceCallInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TestBufferHandler2 implements BufferHandler
{
    public static final String BUFFER_TYPE_2 = "buffertype1";

    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return BUFFER_TYPE_2.equals( bufferType );
    }

    @Override
    public ServiceCallInfo fromBuffer(Proxy p, Method method, CasualBuffer buffer)
    {
        return null;
    }

    @Override
    public CasualBuffer toBuffer(Object result)
    {
        return null;
    }
}
