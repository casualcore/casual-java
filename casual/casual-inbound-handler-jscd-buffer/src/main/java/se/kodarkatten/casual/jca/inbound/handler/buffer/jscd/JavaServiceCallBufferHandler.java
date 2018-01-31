package se.kodarkatten.casual.jca.inbound.handler.buffer.jscd;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.CasualBufferType;
import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition;
import se.kodarkatten.casual.api.external.json.JsonProvider;
import se.kodarkatten.casual.api.external.json.impl.GsonJscdTypeAdapter;
import se.kodarkatten.casual.jca.inbound.handler.buffer.BufferHandler;
import se.kodarkatten.casual.jca.inbound.handler.buffer.ServiceCallInfo;
import se.kodarkatten.casual.jca.inbound.handler.HandlerException;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

import javax.ejb.Local;
import javax.ejb.Stateless;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static se.kodarkatten.casual.api.external.json.JsonProviderFactory.getJsonProvider;

@Stateless
@Local(BufferHandler.class)
public class JavaServiceCallBufferHandler implements BufferHandler
{
    private static final JsonProvider jp = getJsonProvider();

    @Override
    public boolean canHandleBuffer(String bufferType)
    {
        return CasualBufferType.JSON_JSCD.getName().equals( bufferType );
    }

    @Override
    public ServiceCallInfo fromBuffer(Proxy p, Method m, CasualBuffer buffer)
    {
        if( buffer.getBytes().size() != 1 )
        {
            throw new IllegalArgumentException( "Payload size must be 1 but was " + buffer.getBytes().size() );
        }

        try
        {
            String s = new String(buffer.getBytes().get(0), StandardCharsets.UTF_8);
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
    public CasualBuffer toBuffer(Object result)
    {
        List<byte[]> payload = new ArrayList<>();
        if( result != null )
        {
            payload.add(jp.toJson(result).getBytes(StandardCharsets.UTF_8));
        }

        return ServiceBuffer.of( CasualBufferType.JSON_JSCD.getName(), payload );
    }
}
