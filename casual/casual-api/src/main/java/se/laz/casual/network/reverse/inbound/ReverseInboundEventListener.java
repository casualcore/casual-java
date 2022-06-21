package se.laz.casual.network.reverse.inbound;

public interface ReverseInboundEventListener
{
    void handleReverseInboundEvent(ReverseInboundEvent event, ReverseInboundServer server);
}
