/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder;

public class CasualNWMessageEncoder extends MessageToByteEncoder<CasualNWMessage<?>>
{
    private CasualNWMessageEncoder()
    {}
    public static CasualNWMessageEncoder of()
    {
        return new CasualNWMessageEncoder();
    }
    @Override
    protected void encode(ChannelHandlerContext ctx, CasualNWMessage<?> msg, ByteBuf out) throws Exception
    {
        // failure is already handled in NettyNetworkConnection::request
        CasualMessageEncoder.write(out, msg);
    }

    @Override
    public boolean acceptOutboundMessage(Object msg)
    {
        return msg instanceof CasualNWMessage;
    }

}
