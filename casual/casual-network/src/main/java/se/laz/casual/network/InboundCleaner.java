/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import io.netty.channel.Channel;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.domain.DomainDisconnectRequestMessage;

import java.util.Objects;
import java.util.UUID;

public class InboundCleaner
{
    public static void sendDomainDisconnect(Channel channel)
    {
        Objects.requireNonNull(channel,"channel can not be null" );
        channel.writeAndFlush(createMessage());
    }

    private static CasualNWMessage<DomainDisconnectRequestMessage> createMessage()
    {
        DomainDisconnectRequestMessage msg = DomainDisconnectRequestMessage.of(UUID.randomUUID());
        return CasualNWMessageImpl.of(UUID.randomUUID(), msg);
    }
}
