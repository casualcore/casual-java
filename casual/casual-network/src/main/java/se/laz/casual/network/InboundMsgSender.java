/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import io.netty.channel.Channel;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectRequestMessage;
import se.laz.casual.network.protocol.messages.domain.DomainDiscoveryTopologyUpdateMessage;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class InboundMsgSender
{
    private static final Logger log = Logger.getLogger(InboundMsgSender.class.getName());
    private InboundMsgSender()
    {}
    public static void sendDomainDisconnect(Channel channel)
    {
        Objects.requireNonNull(channel,"channel can not be null" );
        channel.writeAndFlush(createDomainDisconnectMessage());
        log.finest(() -> "domain disconnect request sent to " + channel);
    }

    public static void sendDomainDiscoveryImplicitUpdate(Channel channel, UUID executionId)
    {
        Objects.requireNonNull(channel,"channel can not be null" );
        Objects.requireNonNull(executionId,"executionId can not be null" );
        channel.writeAndFlush(createDomainTopologyUpdateMessage(executionId));
        log.finest(() -> "sent domain discovery topology update message to " + channel + " with executionId " + executionId);
    }

    private static CasualNWMessage<DomainDisconnectRequestMessage> createDomainDisconnectMessage()
    {
        DomainDisconnectRequestMessage msg = DomainDisconnectRequestMessage.of(UUID.randomUUID());
        return CasualNWMessageImpl.of(UUID.randomUUID(), msg);
    }

    private static CasualNWMessage<DomainDiscoveryTopologyUpdateMessage> createDomainTopologyUpdateMessage(UUID executionId)
    {
        DomainDiscoveryTopologyUpdateMessage msg = DomainDiscoveryTopologyUpdateMessage.createBuilder()
                                                                                       .withExecution(executionId)
                                                                                       .withDomainName(ConfigurationService.getConfiguration(ConfigurationOptions.CASUAL_DOMAIN_NAME))
                                                                                       .withDomainId(ConfigurationService.getConfiguration(ConfigurationOptions.CASUAL_DOMAIN_ID).getId())
                                                                                       .build();
        return CasualNWMessageImpl.of(UUID.randomUUID(), msg);
    }
}
