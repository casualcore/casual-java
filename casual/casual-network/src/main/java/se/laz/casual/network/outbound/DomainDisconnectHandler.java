/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectReplyMessage;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DomainDisconnectHandler
{
    private static final Logger LOG = Logger.getLogger(DomainDisconnectHandler.class.getName());
    private final AtomicBoolean domainDisconnected = new AtomicBoolean(false);
    private final Channel channel;
    private final Supplier<DomainId> domainIdSupplier;
    private Consumer<DomainDisconnectReplyInfo> domainDisconnectReplyFunction;

    private DomainDisconnectHandler(Channel channel, Supplier<DomainId> domainIdSupplier)
    {
        this.channel = channel;
        this.domainIdSupplier = domainIdSupplier;
    }

    /**
     * Creates a handler for domain-disconnect requests.
     *
     * <p>The handler evaluates {@code domainIdSupplier} only after the decoder has processed the
     * domain-connect reply.
     *
     * @param channel the channel used to send the domain-disconnect reply
     * @param domainIdSupplier the connected peer's domain ID supplier
     * @return a domain-disconnect handler
     * @throws NullPointerException if {@code channel} or {@code domainIdSupplier} is {@code null}
     */
    public static DomainDisconnectHandler of(Channel channel, Supplier<DomainId> domainIdSupplier)
    {
        Objects.requireNonNull(channel, "channel can not be null");
        Objects.requireNonNull(domainIdSupplier, "domainIdSupplier can not be null");
        return new DomainDisconnectHandler(channel, domainIdSupplier);
    }

    public void setDomainDisconnectReplyFunction(Consumer<DomainDisconnectReplyInfo> domainDisconnectReplyFunction)
    {
        this.domainDisconnectReplyFunction = domainDisconnectReplyFunction;
    }

    public Consumer<DomainDisconnectReplyInfo> getDomainDisconnectReplyFunction()
    {
        return null == domainDisconnectReplyFunction ? this::sendDomainDisconnectReply : domainDisconnectReplyFunction;
    }

    public void domainDisconnected(DomainDisconnectReplyInfo domainDisconnectReplyInfo)
    {
        domainDisconnected.set(true);
        getDomainDisconnectReplyFunction().accept(domainDisconnectReplyInfo);
    }

    public boolean hasDomainBeenDisconnected()
    {
        return domainDisconnected.get();
    }

    private void sendDomainDisconnectReply(DomainDisconnectReplyInfo domainDisconnectReplyInfo)
    {
        DomainDisconnectReplyMessage replyMessage = DomainDisconnectReplyMessage.of(domainDisconnectReplyInfo.getExecution());
        try
        {
            ChannelFuture cf = channel.writeAndFlush(CasualNWMessageImpl.of(domainDisconnectReplyInfo.getCorrid(), replyMessage));
            cf.addListener(v -> {
                if(!v.isSuccess()){
                    LOG.log(Level.INFO, v.cause(),
                            () -> "failed sending domain disconnect reply to domain: " + domainIdSupplier.get() + " , this means that casual sent domain disconnect and then went away before we could send domain disconnect reply - this is ok");
                }
            });
        }
        catch(Exception e)
        {
            // if we did not manage to send the domain disconnect message it is due to the connection being gone
            // which is fine
            LOG.log(Level.INFO, e,
                    () -> "could not send domain disconnect reply to domain: " + domainIdSupplier.get() + " , this means that casual sent domain disconnect and then went away before we could send domain disconnect reply - this is ok" );
        }
    }

}
