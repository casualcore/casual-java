/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.utils;

import se.laz.casual.api.CasualRuntimeException;

public class DecoderReaderValidator
{
    private DecoderReaderValidator()
    {
    }

    public static void throwIfDataNotFullyRead( int currentOffset, int dataLength )
    {
        if( currentOffset != dataLength )
        {
            throw new CasualRuntimeException( "Network data was not fully read: " + currentOffset + " of " + dataLength + "." );
        }
    }
}
