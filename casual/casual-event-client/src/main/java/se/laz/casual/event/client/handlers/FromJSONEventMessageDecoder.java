package se.laz.casual.event.client.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.event.ServiceCallEvent;
import se.laz.casual.event.client.EventObserver;

import java.util.logging.Logger;

public class FromJSONEventMessageDecoder extends SimpleChannelInboundHandler<Object>
{
    private static final Logger LOG = Logger.getLogger(FromJSONEventMessageDecoder.class.getName());
    private final EventObserver observer;

    private FromJSONEventMessageDecoder(EventObserver observer)
    {
        this.observer = observer;
    }
    public static FromJSONEventMessageDecoder of(EventObserver observer)
    {
        return new FromJSONEventMessageDecoder(observer);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg)
    {
        ByteBuf content = (ByteBuf)msg;
        String json = content.toString(CharsetUtil.UTF_8);
        ServiceCallEvent event =  JsonProviderFactory.getJsonProvider().fromJson(json, ServiceCallEvent.class);
        observer.notify(event);
        LOG.finest(() -> "read msg: " + event + " on channel: " + channelHandlerContext.channel());
    }
}
