package se.laz.casual.network.grpc.outbound;

import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.messages.CasualReply;

public final class ReplyConverter
{
    private ReplyConverter()
    {}

    public static <T extends CasualNetworkTransmittable> CasualNWMessage<T> toCasualNWMessage(CasualReply reply)
    {
        return null;
    }
}
