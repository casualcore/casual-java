package se.laz.casual.network.grpc.inbound;

import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;

public interface RequestDelegate
{
    CasualReply handleRequest(CasualRequest request);
}
