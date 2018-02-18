package se.kodarkatten.casual.api.network.protocol.messages;

import java.util.List;
import java.util.UUID;

public interface CasualNWMessage<T extends CasualNetworkTransmittable>
{
    CasualNWMessageType getType();
    List<byte[]> toNetworkBytes();
    UUID getCorrelationId();
    T getMessage();
}
