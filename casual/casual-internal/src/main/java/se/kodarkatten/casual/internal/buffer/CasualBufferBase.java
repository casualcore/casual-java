package se.kodarkatten.casual.internal.buffer;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

import java.util.List;

/**
 * @author jone
 */
public class CasualBufferBase<T> implements CasualBuffer
{
    private final ServiceBuffer internalState;

    private CasualBufferBase(ServiceBuffer state)
    {
        internalState = state;
    }

    public final static <X> CasualBufferBase<X> of(ServiceBuffer serviceBuffer, Class<X> bufferClass)
    {
        return new CasualBufferBase<X>(serviceBuffer);
    }

    @Override
    public String getType()
    {
        return internalState.getType();
    }

    @Override
    public List<byte[]> getPayload()
    {
        return internalState.getPayload();
    }
}
