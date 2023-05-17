/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;

public interface CasualOutboundMessageListener
{
    boolean isInterestedIn(CasualNWMessageType type);
    <T extends CasualNetworkTransmittable> void handleMessage(CasualNWMessage<T> message);
}
