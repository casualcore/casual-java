package se.laz.casual.network.outbound;

import java.util.UUID;

public interface DomainDisconnectListener
{
    void domainDisconnecting(NetworkConnectionId id, UUID execution);
}
