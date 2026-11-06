/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.jca.DomainId;

public interface CasualOutboundMessageListener
{
    /**
     * Initializes the listener with the connected peer's domain ID.
     *
     * <p>The message handler calls this method on the channel event-loop thread before it
     * completes the correlated domain-connect reply.
     *
     * @param domainId the connected peer's domain ID
     * @throws NullPointerException if {@code domainId} is {@code null}
     */
    void domainConnected(DomainId domainId);
    boolean isInterestedIn(CasualNWMessageType type);
    <T extends CasualNetworkTransmittable> void handleMessage(CasualNWMessage<T> message);
}
