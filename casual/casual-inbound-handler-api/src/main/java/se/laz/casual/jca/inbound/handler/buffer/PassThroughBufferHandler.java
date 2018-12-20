/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static se.laz.casual.jca.inbound.handler.buffer.DispatchMethodUtil.methodAccepts;
import static se.laz.casual.jca.inbound.handler.buffer.DispatchMethodUtil.toMethodParams;

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
            throw new CasualRuntimeException("Unable to perform passthrough as dispatch method does not accept required parameter.");
        }
        return ServiceCallInfo.of( method, params );
    }

    @Override
    public InboundResponse toResponse(Object result)
    {
        if( result instanceof InboundResponse )
        {
            return (InboundResponse) result;
        }
        return InboundResponse.createBuilder().buffer( (CasualBuffer)result).build();
    }
}
