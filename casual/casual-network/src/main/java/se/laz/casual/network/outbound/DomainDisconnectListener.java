package se.laz.casual.network.outbound;

import java.util.UUID;

public interface DomainDisconnectListener
{
    void domainDisconnecting(NettyNetworkConnection networkConnection, UUID execution);
}
