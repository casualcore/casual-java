package se.kodarkatten.casual.network.messages.common;

/**
 * Created by aleph on 2017-03-15.
 */

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
}
