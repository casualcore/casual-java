/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type;

/**
 * Created by aleph on 2017-03-15.
 */

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The actual buffer type used in service call request and reply
 */
public final class ServiceBuffer implements CasualBuffer, Serializable
{
    private static final long serialVersionUID = 1L;
    private static final ServiceBuffer EMPTY_INSTANCE = new ServiceBuffer("", new ArrayList<>());
    private static final ServiceBuffer NULL_INSTANCE = new ServiceBuffer("NULL", new ArrayList<>());
    private String type;
    private List<byte[]> payload;
    private ServiceBuffer(final String type, final List<byte[]> payload)
    {
        this.type = type;
        this.payload = payload;
    }

    /**
     * Creates a new {@link ServiceBuffer}
     * Note, since payload can be large we do not copy it - ie ownership is implicitly transferred
     * Be aware
     * @param type the type of the buffer
     * @param payload the payload
     * @return a new Buffer
     */
    public static ServiceBuffer of(final String type, final List<byte[]> payload)
    {
        Objects.requireNonNull(type, "type can not be null");
        Objects.requireNonNull(payload, "bytes can not be null");
        if(type.isEmpty() && payload.isEmpty())
        {
            return EMPTY_INSTANCE;
        }
        if(notValidBufferData(type, payload))
        {
            throw new CasualProtocolException("invalid buffer data,  type is empty but there is a payload");
        }
        return new ServiceBuffer(type, payload);
    }

    public static ServiceBuffer empty()
    {
        return EMPTY_INSTANCE;
    }

    public static ServiceBuffer nullBuffer()
    {
        return NULL_INSTANCE;
    }

    private static boolean notValidBufferData(String type, List<byte[]> payload)
    {
        // There is only one case that is not valid:
        // No type but a payload
        return (!payload.isEmpty() && type.isEmpty());
    }

    public boolean isEmpty()
    {
        return type.isEmpty() && payload.isEmpty();
    }

    /**
     * Creates a new {@link ServiceBuffer} based on any other CasualBuffer
     * @param b the buffer
     * @return a new ServiceBuffer
     */
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
     * @return the payload
     */
    public List<byte[]> getPayload()
    {
        return Collections.unmodifiableList(payload);
    }

    /**
     * Always returns a list of at least two byte[]
     * The first one contains the type name encoded as utf-8
     * The rest of the items are the payload
     * @return the data to send over the network
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
