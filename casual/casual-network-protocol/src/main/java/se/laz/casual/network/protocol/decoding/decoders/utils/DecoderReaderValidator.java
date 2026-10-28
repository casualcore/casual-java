/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.utils;

import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;

public class DecoderReaderValidator
{
    private DecoderReaderValidator()
    {
    }

    /**
     * Verifies that a decoder reads all available network data when strict network validation is enabled.
     *
     * @param currentOffset number of bytes that the decoder reads.
     * @param dataLength number of bytes available to the decoder.
     * @throws CasualProtocolException if strict network validation is enabled and the decoder does not read all available data.
     */
    public static void throwIfDataNotFullyRead( int currentOffset, int dataLength )
    {
        if( Boolean.FALSE.equals( ConfigurationService.getConfiguration(
                ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_VALIDATION) ) )
        {
            // the reason for this is interoperability with older versions where
            // some messages contain 8 trailing bytes
            return;
        }
        if( currentOffset != dataLength )
        {
            throw new CasualProtocolException( "Network data was not fully read: " + currentOffset + " of " + dataLength + "." );
        }
    }
}
