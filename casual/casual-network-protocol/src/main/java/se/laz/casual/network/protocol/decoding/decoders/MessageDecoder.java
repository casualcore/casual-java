/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders;

import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;

import java.util.Objects;

/**
 * Created by aleph on 2017-03-28.
 */
public final class MessageDecoder<T extends CasualNetworkTransmittable>
{
    final NetworkDecoder<T> networkDecoder;
    final int maxSingleBufferByteSize;

    private MessageDecoder(final NetworkDecoder<T> networkDecoder, int maxSingleBufferByteSize)
    {
        this.networkDecoder = networkDecoder;
        this.maxSingleBufferByteSize = maxSingleBufferByteSize;
    }

    public static <T extends CasualNetworkTransmittable> MessageDecoder<T> of(final NetworkDecoder<T> r)
    {
        return of(r, Integer.MAX_VALUE);
    }

    public static <T extends CasualNetworkTransmittable> MessageDecoder<T> of(final NetworkDecoder<T> r, int maxSingleBufferByteSize)
    {
        Objects.requireNonNull(r, "networkDecoder can not be null!");
        return new MessageDecoder<>(r, maxSingleBufferByteSize);
    }

    public T read(final byte[] data)
    {
        Objects.requireNonNull(data, "data is null");
        return networkDecoder.readSingleBuffer(data);
    }

}
