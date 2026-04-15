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
    private final NetworkDecoder<T> networkDecoder;

    private MessageDecoder(final NetworkDecoder<T> networkDecoder)
    {
        this.networkDecoder = networkDecoder;
    }

    public static <T extends CasualNetworkTransmittable> MessageDecoder<T> of(final NetworkDecoder<T> r)
    {
        return new MessageDecoder<>(r);
    }

    public T read(final byte[] data)
    {
        Objects.requireNonNull(data, "data is null");
        return networkDecoder.readSingleBuffer(data);
    }

}
