package se.laz.casual.event.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.CharsetUtil;
import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.event.server.messages.LogonRequestMessage;
import se.laz.casual.event.server.messages.LogonRequestTypeAdapter;

import java.util.Objects;
import java.util.logging.Logger;

public class FromJSONLogonDecoder extends SimpleChannelInboundHandler<Object>
{
    private static final Logger log = Logger.getLogger(FromJSONLogonDecoder.class.getName());
    private final ChannelGroup connectedClients;

    private FromJSONLogonDecoder(ChannelGroup connectedClients)
    {
        this.connectedClients = connectedClients;
    }

    public static FromJSONLogonDecoder of(ChannelGroup connectedClients)
    {
        Objects.requireNonNull(connectedClients, "connectedClients can not be null");
        return new FromJSONLogonDecoder(connectedClients);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        ByteBuf content = (ByteBuf)msg;
        String json = content.toString(CharsetUtil.UTF_8);
        log.info(() -> "msg json: " + json);
        LogonRequestMessage requestMessage = JsonProviderFactory.getJsonProvider().fromJson(json, LogonRequestMessage.class, LogonRequestTypeAdapter.of());
        connectedClients.add(ctx.channel());
        ctx.fireChannelRead(requestMessage);
        log.info(() -> "client logged on: " + requestMessage);
    }
}
