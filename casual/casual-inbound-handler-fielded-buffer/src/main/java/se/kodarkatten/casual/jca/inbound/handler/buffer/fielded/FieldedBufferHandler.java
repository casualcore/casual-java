package se.kodarkatten.casual.jca.inbound.handler.buffer.fielded;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.CasualBufferType;
import se.kodarkatten.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor;
import se.kodarkatten.casual.jca.inbound.handler.buffer.BufferHandler;
import se.kodarkatten.casual.jca.inbound.handler.buffer.ServiceCallInfo;

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

        Object[] params = FieldedTypeBufferProcessor.unmarshall( fieldedBuffer, method );

        return ServiceCallInfo.of( method, params );
    }

    @Override
    public CasualBuffer toBuffer(Object result)
    {
        FieldedTypeBuffer buffer = FieldedTypeBuffer.create();
        if( result != null )
        {
            buffer = FieldedTypeBufferProcessor.marshall(result);
        }
        return buffer;
    }
}
