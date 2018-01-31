package se.kodarkatten.casual.network.messages.service;

/**
 * Created by aleph on 2017-03-15.
 */

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Used in service call request and reply
 */
public final class ServiceBuffer implements CasualBuffer, Serializable
{
    private static final long serialVersionUID = 1L;
    private String type;
    private List<byte[]> payload;
    private ServiceBuffer(final String type, final List<byte[]> payload)
    {
        this.type = type;
        this.payload = payload;
    }

    /**
     * Note, since payload can be large we do not copy it - ie ownership is implicitly transferred
     * Be aware
     * @param type
     * @param bytes
     * @return
     */
    public static ServiceBuffer of(final String type, final List<byte[]> bytes)
    {
        Objects.requireNonNull(type, "type can not be null");
        Objects.requireNonNull(bytes, "bytes can not be null");
        if(type.isEmpty())
        {
            throw new CasualTransportException("type can not be and empty string");
        }
        return new ServiceBuffer(type, bytes);
    }

    public static ServiceBuffer of(final CasualBuffer b)
    {
        Objects.requireNonNull(b, "buffer can not be null");
        return ServiceBuffer.of(b.getType(), b.getBytes());
    }

    @Override
    public String getType()
    {
        return type;
    }

    @Override
    public List<byte[]> getBytes()
    {
        return getPayload();
    }

    /**
     * Note, since payload can be very large we return the actual payload and not a copy
     * Be aware
     * @return
     */
    public List<byte[]> getPayload()
    {
        return payload;
    }

    /**
     * Always returns a list of at least two byte[]
     * The first one contains the type name encoded as utf-8
     * The rest of the items are the payload
     * @return
     */
    public List<byte[]> toNetworkBytes()
    {
        final byte[] typeBytes = type.getBytes(StandardCharsets.UTF_8);
        final List<byte[]> r = new ArrayList<>();
        r.add(typeBytes);
        r.addAll(getPayload());
        return r;
    }
}
