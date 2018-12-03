/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer.fielded;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.CasualBufferType;
import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo;

import javax.ejb.Local;
import javax.ejb.Stateless;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Stateless
@Local(BufferHandler.class)
public class FieldedBufferHandler implements BufferHandler
{
    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return CasualBufferType.FIELDED.getName().equals( bufferType );
    }

    @Override
    public ServiceCallInfo fromBuffer(Proxy p, Method method, CasualBuffer buffer)
    {
        FieldedTypeBuffer fieldedBuffer = FieldedTypeBuffer.create( buffer.getBytes() );
        Object[] params;
        if( methodAcceptsBuffer( method, buffer ) )
        {
            params = new Object[]{ method.getParameterTypes()[0].cast( buffer ) };
        }
        else
        {
            params = FieldedTypeBufferProcessor.unmarshall(fieldedBuffer, method);
        }

        return ServiceCallInfo.of( method, params );
    }

    private boolean methodAcceptsBuffer(Method method, CasualBuffer buffer )
    {
        return method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom( buffer.getClass() );
    }

    @Override
    public CasualBuffer toBuffer(Object result)
    {
        FieldedTypeBuffer buffer = FieldedTypeBuffer.create();
        if( result != null )
        {
            if( result instanceof CasualBuffer )
            {
                return (CasualBuffer)result;
            }

            buffer = FieldedTypeBufferProcessor.marshall(result);
        }
        return buffer;
    }
}
