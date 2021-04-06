/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.laz.casual.jca.inflow.CasualMessageListener;
import se.laz.casual.network.messages.CasualRequest;

import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import java.util.Objects;
import java.util.logging.Logger;

@ChannelHandler.Sharable
public final class CasualMessageHandler extends SimpleChannelInboundHandler<CasualRequest>
{
    private static Logger log = Logger.getLogger(CasualMessageHandler.class.getName());
    private final MessageEndpointFactory factory;
    private final XATerminator xaTerminator;
    private final WorkManager workManager;

    private CasualMessageHandler(MessageEndpointFactory factory, XATerminator xaTerminator, WorkManager workManager)
    {
        this.factory = factory;
        this.xaTerminator = xaTerminator;
        this.workManager = workManager;
    }

    public static CasualMessageHandler of(final MessageEndpointFactory factory, final XATerminator xaTerminator, final WorkManager workManager)
    {
        Objects.requireNonNull(factory, "factory can not be null");
        Objects.requireNonNull(xaTerminator, "xaTerminator can not be null");
        Objects.requireNonNull(workManager, "workManager can not be null");
        return new CasualMessageHandler(factory, xaTerminator, workManager);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CasualRequest message) throws Exception
    {
        MessageEndpoint endpoint = factory.createEndpoint(null);
        CasualMessageListener listener = (CasualMessageListener) endpoint;
        switch ( message.getMessageType() )
        {
            case COMMIT_REQUEST:
                listener.commitRequest(message, ctx.channel(), xaTerminator);
                break;
            case PREPARE_REQUEST:
                listener.prepareRequest(message, ctx.channel(), xaTerminator);
                break;
            case ROLLBACK_REQUEST:
                listener.requestRollback(message, ctx.channel(), xaTerminator);
                break;
            case SERVICE_CALL_REQUEST:
                listener.serviceCallRequest(message, ctx.channel(), workManager);
                break;
            case DOMAIN_CONNECT_REQUEST:
                listener.domainConnectRequest(message, ctx.channel());
                break;
            case DOMAIN_DISCOVERY_REQUEST:
                listener.domainDiscoveryRequest(message, ctx.channel());
                break;
            default:
                log.warning("Message type not supported: " + message.getMessageType());
        }
    }

}
