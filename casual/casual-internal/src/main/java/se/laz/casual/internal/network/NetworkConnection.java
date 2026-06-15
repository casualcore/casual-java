/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.internal.network;

import se.laz.casual.api.conversation.ConversationClose;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.jca.ConnectionObserver;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.protocol.messages.conversation.Request;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created by aleph on 2017-06-14.
 */
public interface NetworkConnection
{
    <T extends CasualNetworkTransmittable, X extends CasualNetworkTransmittable> CompletableFuture<CasualNWMessage<T>> request(CasualNWMessage<X> message);
    <X extends CasualNetworkTransmittable> void requestNoReply(CasualNWMessage<X> message);

    <X extends CasualNetworkTransmittable> void send(CasualNWMessage<X> message);
    CompletableFuture<CasualNWMessage<Request>> receive(UUID corrid);

    ConversationClose getConversationClose();

    void close();
    boolean isActive();

    DomainId getDomainId();

    void addConnectionObserver(ConnectionObserver observer);

    ProtocolVersion getProtocolVersion();

    /**
     * The domain is in the process of disconnecting
     * IE the server side has sent a domain disconnect message to the client
     *
     * @return true if the connection is disconnecting, false if not
     */
    boolean isDomainDisconnecting();
}
