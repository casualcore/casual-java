/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.network.protocol.messages;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Interface for message that can be sent on the wire
 * Created by aleph on 2017-03-09.
 */
public interface CasualNetworkTransmittable
{
    CasualNWMessageType getType();
    List<byte[]> toNetworkBytes();
    default List<ByteBuffer> toNetworkByteBuffers()
    {
        return toNetworkBytes().stream().map( ByteBuffer::wrap ).toList();
    }
}
