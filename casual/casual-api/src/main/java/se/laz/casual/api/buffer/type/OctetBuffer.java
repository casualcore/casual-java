/*
 * Copyright (c) 2021 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.CasualBufferType;
import se.laz.casual.api.buffer.CasualHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OctetBuffer implements CasualBuffer
{
    private static final long serialVersionUID = 1L;
    private final List<byte[]> data;
    private final CasualHeaders headers;

    private OctetBuffer(List<byte[]> data, CasualHeaders headers )
    {
        this.data = data;
        this.headers = headers;
    }

    /**
     * Create an {@link OctetBuffer} from the data with empty headers.
     *
     * @param data the buffer data.
     * @return a new buffer.
     */
    public static OctetBuffer of(byte[] data)
    {
        return of( data, CasualHeaders.empty() );
    }

    /**
     * Create an {@link OctetBuffer} from the data with headers.
     *
     * @param data the buffer data.
     * @param headers the headers.
     * @return a new buffer.
     */
    public static OctetBuffer of( byte[] data, CasualHeaders headers )
    {
        Objects.requireNonNull(data, "data can not be null");
        List<byte[]> l = new ArrayList<>();
        l.add(data);
        return of(l, headers );
    }

    /**
     * Create an {@link OctetBuffer} from the data with empty headers.
     *
     * @param data the buffer data.
     * @return a new buffer.
     */
    public static OctetBuffer of(List<byte[]> data)
    {
        return of( data, CasualHeaders.empty() );
    }

    /**
     * Create an {@link OctetBuffer} from the data with headers.
     *
     * @param data the buffer data.
     * @param headers the headers.
     * @return a new buffer.
     */
    public static OctetBuffer of(List<byte[]> data, CasualHeaders headers )
    {
        Objects.requireNonNull(data, "data can not be null");
        Objects.requireNonNull( headers, "headers is null." );
        return new OctetBuffer(data, headers );
    }

    @Override
    public String getType()
    {
        return CasualBufferType.X_OCTET.getName();
    }

    @Override
    public List<byte[]> getBytes()
    {
        return Collections.unmodifiableList(data);
    }

    @Override
    public CasualHeaders getHeaders()
    {
        return this.headers;
    }
}
