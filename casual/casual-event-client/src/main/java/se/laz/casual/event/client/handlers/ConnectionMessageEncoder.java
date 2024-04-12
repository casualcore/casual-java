package se.laz.casual.event.client.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import se.laz.casual.event.client.messages.ConnectionMessage;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ConnectionMessageEncoder extends MessageToByteEncoder<ConnectionMessage>
{
    private static final Logger LOG = Logger.getLogger(ConnectionMessageEncoder.class.getName());
    private ConnectionMessageEncoder()
    {}

    public static ConnectionMessageEncoder of()
    {
        return new ConnectionMessageEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ConnectionMessage connectionMessage, ByteBuf byteBuf)
    {
        LOG.finest(() -> "about to write: " + connectionMessage);
        byteBuf.writeBytes(connectionMessage.getConnectionMessage().getBytes(StandardCharsets.UTF_8));
        LOG.finest(() -> "wrote: " + connectionMessage);
    }
    @Override
    public boolean acceptOutboundMessage(Object msg)
    {
        return msg instanceof ConnectionMessage;
    }
}
