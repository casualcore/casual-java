/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer.fielded;

import se.laz.casual.api.buffer.CasualBufferType;
import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo;

import javax.ejb.Stateless;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static se.laz.casual.jca.inbound.handler.buffer.DispatchMethodUtil.methodAccepts;
import static se.laz.casual.jca.inbound.handler.buffer.DispatchMethodUtil.toMethodParams;

@Stateless
public class FieldedBufferHandler implements BufferHandler
{
    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return CasualBufferType.FIELDED.getName().equals( bufferType );
    }

    @Override
    public ServiceCallInfo fromRequest(Proxy p, Method method, InboundRequest request)
    {
        Object[] params;
        if( methodAccepts( method, request ) )
        {
            params = toMethodParams( method, request );
        }
        else if( methodAccepts( method, request.getBuffer() ) )
        {
            params = toMethodParams( method, request.getBuffer() );
        }
        else
        {
            FieldedTypeBuffer fieldedBuffer = FieldedTypeBuffer.create( request.getBuffer().getBytes() );
            params = FieldedTypeBufferProcessor.unmarshall(fieldedBuffer, method);
        }

        return ServiceCallInfo.of( method, params );
    }

    @Override
    public InboundResponse toResponse(ServiceCallInfo info, Object result)
    {
        FieldedTypeBuffer buffer = FieldedTypeBuffer.create();
        if( result != null )
        {
            if( result instanceof InboundResponse )
            {
                return (InboundResponse) result;
            }

            buffer = FieldedTypeBufferProcessor.marshall(result);
        }
        return InboundResponse.createBuilder().buffer(buffer).build();
    }
}
