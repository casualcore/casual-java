/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.encoding;


import io.netty.buffer.ByteBuf;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Created by aleph on 2017-03-12.
 */
public final class CasualMessageEncoder
{
    private CasualMessageEncoder()
    {}

    public static <T extends CasualNetworkTransmittable> void write(final WritableByteChannel channel, final CasualNWMessage<T> msg)
    {
        for(final byte[] bytes : msg.toNetworkBytes())
        {
            ByteUtils.writeFully(channel, ByteBuffer.wrap(bytes), bytes.length);
        }
    }

    public static <T extends CasualNetworkTransmittable> void write(final ByteBuf out, final CasualNWMessage<T> msg)
    {
        for(byte[] b : msg.toNetworkBytes())
        {
            out.writeBytes(b);
        }
    }
}
