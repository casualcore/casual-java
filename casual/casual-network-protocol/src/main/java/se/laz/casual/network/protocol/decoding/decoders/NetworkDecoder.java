/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders;

import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;

/**
 * Created by aleph on 2017-03-28.
 */
public interface NetworkDecoder<T extends CasualNetworkTransmittable>
{
    /**
     * Used in production!!!!!
     * @param data the network data.
     * @return the object
     */
    T readSingleBuffer(final byte[] data);
}
