package se.kodarkatten.casual.network.messages.service;

/**
 * Created by aleph on 2017-03-15.
 */

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Used in service call request and reply
 */
public final class ServiceBuffer
{
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
        return new ServiceBuffer(type, bytes);
    }

    public String getType()
    {
        return type;
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

    /**
     * Since paylods can be huge, we never ever compare them
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ServiceBuffer that = (ServiceBuffer) o;
        return Objects.equals(type, that.type);
    }

    /**
     * Be aware that payload can be huge
     * @return
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(type, payload);
    }
}
