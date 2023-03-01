/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer.jscd;

import se.laz.casual.api.buffer.CasualBufferType;
import se.laz.casual.api.buffer.type.JavaServiceCallDefinition;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.external.json.JsonProvider;
import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.api.external.json.impl.GsonJscdTypeAdapter;
import se.laz.casual.jca.inbound.handler.HandlerException;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaServiceCallBufferHandler implements BufferHandler
{
    private static final JsonProvider jp = JsonProviderFactory.getJsonProvider();

    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return CasualBufferType.JSON_JSCD.getName().equals( bufferType );
    }

    @Override
    public ServiceCallInfo fromRequest(Proxy p, Method m, InboundRequest request)
    {
        if( request.getBuffer().getBytes().size() != 1 )
        {
            throw new IllegalArgumentException( "Payload size must be 1 but was " + request.getBuffer().getBytes().size() );
        }

        try
        {
            String s = new String(request.getBuffer().getBytes().get(0), StandardCharsets.UTF_8);
            JavaServiceCallDefinition callDef = jp.fromJson(s, JavaServiceCallDefinition.class, new GsonJscdTypeAdapter());

            String[] methodParamTypes = callDef.getMethodParamTypes();
            Class<?>[] params = new Class<?>[methodParamTypes.length];
            for (int i = 0; i < methodParamTypes.length; i++)
            {
                params[i] = Class.forName(methodParamTypes[i], true, Thread.currentThread().getContextClassLoader());
            }
            Method method = p.getClass().getMethod(callDef.getMethodName(), params);

            return ServiceCallInfo.of(method, callDef.getMethodParams());
        }
        catch( ClassNotFoundException | NoSuchMethodException e )
        {
            throw new HandlerException( "Error with buffer transformation.",e );
        }
    }

    @Override
    public InboundResponse toResponse(ServiceCallInfo info, Object result)
    {
        List<byte[]> payload = new ArrayList<>();
        if( result != null )
        {
            payload.add(jp.toJson(result).getBytes(StandardCharsets.UTF_8));
        }
        final String typename = payload.isEmpty() ? "" : CasualBufferType.JSON_JSCD.getName();
        return InboundResponse.createBuilder().buffer( ServiceBuffer.of( typename, payload ) ).build();
    }
}
