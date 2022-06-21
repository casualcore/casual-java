package se.laz.casual.network.reverse.inbound;

public interface ReverseInboundListener
{
    void disconnected(ReverseInboundServer server);
    void connected(ReverseInboundServer server);
}
