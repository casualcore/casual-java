/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.internal.network;

import se.laz.casual.api.conversation.ConversationClose;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.messages.conversation.Request;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created by aleph on 2017-06-14.
 */
public interface NetworkConnection
{
    <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message);

    <X extends CasualNetworkTransmittable> void send(CasualNWMessage<X> message);
    CompletableFuture<CasualNWMessage<Request>> receive(UUID corrid);

    ConversationClose getConversationClose();

    void close();
    boolean isActive();
}
