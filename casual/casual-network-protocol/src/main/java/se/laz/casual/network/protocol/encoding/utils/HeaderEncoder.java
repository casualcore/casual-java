/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.encoding.utils;

import se.laz.casual.api.buffer.CasualHeaders;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Assist with encoding of maps of headers to casual network bytes format.
 */
public final class HeaderEncoder
{

    private HeaderEncoder()
    {
    }

    /**
     * Convert a map to "key:value" strings and then encoded them to UTF-8 byte[]s.
     *
     * @param headers to convert into byte[]s.
     * @return list of key:value strings for each map entry.
     */
    public static List<byte[]> convertMapToBytes( CasualHeaders headers )
    {
        return headers.getAll().stream()
                .map( s-> s.getBytes(StandardCharsets.UTF_8) )
                .toList();
    }

    /**
     * Write the headers into the provided {@link ByteBuffer} in the casual network protocol format:
     * <pre>
     * header.size - the size of the headers param list.
     * header.element.size - the size of each list item byte[].
     * header.element.data - the header byte[].
     * </pre>
     * @param target where to write the headers.
     * @param headers the header bytes to write.
     */
    public static void writeHeaderBytes( ByteBuffer target, List<byte[]> headers )
    {
        target.putLong( headers.size() );
        for( byte[] header: headers )
        {
            target.putLong( header.length );
            target.put( header );
        }
    }

    /**
     * Encode raw headers in the casual network protocol format:
     * <pre>
     * header.size - the size of the headers param list.
     * header.element.size - the size of each list item byte[].
     * header.element.data - the header byte[].
     * </pre>
     * @param headers the header bytes to write.
     * @return list of network byte[]s for the provided headers.
     */
    public static List<byte[]> encodeHeadersBytesAsList( List<byte[]> headers )
    {
        List<byte[]> l = new ArrayList<>();
        l.add( CasualEncoderUtils.writeLong( headers.size() ) );
        for( byte[] header: headers )
        {
            l.add( CasualEncoderUtils.writeLong( header.length ) );
            l.add( header );
        }
        return l;
    }
}
