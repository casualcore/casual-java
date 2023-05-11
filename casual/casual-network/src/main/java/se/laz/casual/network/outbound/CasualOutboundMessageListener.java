package se.laz.casual.network.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;

public interface CasualOutboundMessageListener
{
    boolean isInterestedIn(CasualNWMessageType type);
    <T extends CasualNetworkTransmittable> void handleMessage(CasualNWMessage<T> message);
}
