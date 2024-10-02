/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.jca.inbound.handler.HandlerException;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;

import java.lang.reflect.Method;

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
    public ServiceCallInfo fromRequest(InboundRequestInfo requestInfo, InboundRequest request)
    {
        Object[] params;
        Method proxyMethod = requestInfo.getProxyMethod().orElseThrow(() -> new InboundRequestException("Missing proxy method, requestInfo: " + requestInfo));
        if( methodAccepts( proxyMethod, request ) )
        {
            params = toMethodParams( proxyMethod, request );
        }
        else if( methodAccepts( proxyMethod, request.getBuffer() ) )
        {
            params = toMethodParams( proxyMethod, request.getBuffer() );
        }
        else
        {
            throw new HandlerException("Unable to perform passthrough as dispatch method does not accept required parameter.");
        }
        return ServiceCallInfo.of( proxyMethod, params );
    }

    @Override
    public InboundResponse toResponse(ServiceCallInfo info, Object result)
    {
        if( result instanceof InboundResponse response )
        {
            return response;
        }
        return InboundResponse.createBuilder().buffer( (CasualBuffer)result).build();
    }
}
