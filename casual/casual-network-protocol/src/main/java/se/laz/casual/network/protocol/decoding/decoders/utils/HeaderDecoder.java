/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.utils;

import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Assist decoding of headers data from casual network messages.
 */
public final class HeaderDecoder
{

    private HeaderDecoder()
    {
    }

    /**
     * Read the header information from the provided byte[] starting at the
     * provided offset.
     * <br/>
     * Populate the provided map with the resulting headers, split using the
     * first ":".
     *
     * @param data to read header data from.
     * @param currentOffset where to start within the data [].
     * @param target map to put each read header.
     * @return the new offset position in the data [] where reading stopped.
     */
    public static int decodeHeaders( byte[] data, int currentOffset, Map<String,String> target )
    {
        final long headerSize = ByteBuffer.wrap( data, currentOffset, CommonSizes.HEADER_SIZE.getNetworkSize() ).getLong();
        currentOffset += CommonSizes.HEADER_SIZE.getNetworkSize();

        for( int i=0; i<headerSize; i++ )
        {
            long headerElementSize = ByteBuffer.wrap( data, currentOffset, CommonSizes.HEADER_ELEMENT_SIZE.getNetworkSize() ).getLong();
            currentOffset += CommonSizes.HEADER_ELEMENT_SIZE.getNetworkSize();
            String headerElementData = CasualMessageDecoderUtils.getAsString(data, currentOffset, (int)headerElementSize );
            currentOffset += (int)headerElementSize;
            int index = headerElementData.indexOf( ":" );
            target.put( headerElementData.substring( 0, index ), headerElementData.substring( index+1 ) );
        }
        return currentOffset;
    }
}
