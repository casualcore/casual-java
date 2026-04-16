/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;

import java.nio.ByteBuffer;

public class CasualNWMessageEncoder2 extends MessageToByteEncoder<CasualNWMessage<?>>
{
    private CasualNWMessageEncoder2()
    {}
    public static CasualNWMessageEncoder2 of()
    {
        return new CasualNWMessageEncoder2();
    }
    @Override
    protected void encode(ChannelHandlerContext ctx, CasualNWMessage<?> msg, ByteBuf out) throws Exception
    {
        for( ByteBuffer buffer : msg.toNetworkByteBuffers() )
        {
            out.writeBytes( buffer );
        }
    }

    @Override
    public boolean acceptOutboundMessage(Object msg)
    {
        return msg instanceof CasualNWMessage;
    }

}
