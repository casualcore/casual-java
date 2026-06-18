/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.CasualBufferType;
import se.laz.casual.api.buffer.CasualHeaders;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * {@link CasualBuffer} for type {@link CasualBufferType#CSTRING}
 */
public class CStringBuffer implements CasualBuffer
{
    private static final String NULL_TERMINATOR = "\0";
    private static final byte NULL_TERMINATOR_BYTE = 0;

    private static final long serialVersionUID = 1L;
    private final String charset;
    private final byte[] payload;
    private final CasualHeaders headers;

    private CStringBuffer(byte[] payload, String charset, CasualHeaders headers )
    {
        this.payload = payload;
        this.charset = charset;
        this.headers = headers;
    }

    /**
     * Creates a {@link CStringBuffer}
     * Uses the platform's default charset
     * ( Charset.defaultCharset() )
     * @param value - the string value
     * @return a {@link CStringBuffer}
     */
    public static CStringBuffer of(final String value)
    {
        return of(value, Charset.defaultCharset(), CasualHeaders.empty() );
    }

    /**
     * Create a {@link CStringBuffer}
     * with the provided headers.
     * @param value - the string value.
     * @param headers - the headers 
     * @return
     */
    public static CStringBuffer of(final String value, CasualHeaders headers )
    {
        return of( value, Charset.defaultCharset(), headers );
    }


    public static CStringBuffer of(final String value, final Charset charset)
    {
        return of( value, charset, CasualHeaders.empty() );
    }

    public static CStringBuffer of( final String value, final Charset charset, CasualHeaders headers )
    {
        Objects.requireNonNull(value, "value should not be null!");
        Objects.requireNonNull(charset, "charset can not be null");
        Objects.requireNonNull( headers, "headers can not be null" );
        String data = value;
        if (!data.endsWith(NULL_TERMINATOR))
        {
            data += NULL_TERMINATOR;
        }
        return new CStringBuffer(data.getBytes(charset), charset.name(), headers );
    }


    /**
     * Creates a {@link CStringBuffer}
     * @param payload has to contain one byte[] which has to be a null terminated cstring using the default platform encoding
     * @return a {@link CStringBuffer}
     */
    public static CStringBuffer of(final List<byte[]> payload)
    {
        return of(payload, Charset.defaultCharset(), CasualHeaders.empty() );
    }

    public static CStringBuffer of(final List<byte[]> payload, CasualHeaders headers )
    {
        return of( payload, Charset.defaultCharset(), headers );
    }

    public static CStringBuffer of(final List<byte[]> payload, final Charset charset )
    {
        return of( payload, charset, CasualHeaders.empty() );
    }

    public static CStringBuffer of(final List<byte[]> payload, final Charset charset, CasualHeaders headers )
    {
        Objects.requireNonNull(payload, "payload can not be null!");
        Objects.requireNonNull(charset, "charset can not be null");
        Objects.requireNonNull(headers, "headers cannot be null" );
        // Java string can only be created from one byte[] - ie that is the max size of a javastring
        // The payload is expected to be a null terminated c string
        if(payload.size() != 1)
        {
            throw new IllegalArgumentException("the list has to contain only one byte[]");
        }
        byte[] data = payload.get( 0 );
        if( data[data.length-1] != NULL_TERMINATOR_BYTE )
        {
            throw new IllegalArgumentException("the byte[] must be null terminated.");
        }
        return new CStringBuffer(data, charset.name(), headers );
    }

    public Charset getCharset()
    {
        return Charset.forName(charset);
    }

    @Override
    public String getType()
    {
        return CasualBufferType.CSTRING.getName();
    }

    @Override
    public CasualHeaders getHeaders()
    {
        return this.headers;
    }

    @Override
    public List<byte[]> getBytes()
    {
        return Arrays.asList(payload);
    }
    @Override
    public String toString()
    {
        return new String(payload, 0,payload.length - 1, Charset.forName(charset));
    }
}
