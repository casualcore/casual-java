/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders;

import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;

import java.nio.channels.ReadableByteChannel;

/**
 * Created by aleph on 2017-03-28.
 */
public interface NetworkDecoder<T extends CasualNetworkTransmittable>
{
    T readSingleBuffer(final ReadableByteChannel channel, int messageSize);
    T readChunked(final ReadableByteChannel channel);

    T readSingleBuffer(final byte[] data);
}
