package se.laz.casual.network.reverse.inbound;

public interface ReverseInboundListener extends ReverseInboundConnectListener
{
    void disconnected(ReverseInboundServer server);
}
