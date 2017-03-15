package se.kodarkatten.casual.network.messages.common;

/**
 * Created by aleph on 2017-03-15.
 */

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    public ServiceBuffer of(final String type, final List<byte[]> bytes)
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

}
