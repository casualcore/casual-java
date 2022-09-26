package se.laz.casual.jca.pool;

import se.laz.casual.jca.Address;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.outbound.NetworkListener;

@FunctionalInterface
public interface NetworkConnectionCreator
{
    ReferenceCountedNetworkConnection createNetworkConnection(Address address, ProtocolVersion protocolVersion, NetworkListener networkListener, ReferenceCountedNetworkCloseListener referenceCountedNetworkCloseListener);
}
