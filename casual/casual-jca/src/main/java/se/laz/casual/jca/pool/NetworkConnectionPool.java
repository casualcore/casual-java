package se.laz.casual.jca.pool;

import se.laz.casual.api.CasualRuntimeException;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Domain;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.Address;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.outbound.NettyConnectionInformation;
import se.laz.casual.network.outbound.NettyNetworkConnection;
import se.laz.casual.network.outbound.NetworkListener;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class NetworkConnectionPool implements ReferenceCountedNetworkCloseListener
{
    private static final Logger LOG = Logger.getLogger(NetworkConnectionPool.class.getName());
    private final Address address;
    private final List<ReferenceCountedNetworkConnection> connections = new ArrayList<>();
    private int poolSize;
    private final Object connectionLock = new Object();

    private NetworkConnectionPool(Address address, int poolSize)
    {
        this.address = address;
        this.poolSize = poolSize;
    }

    public static NetworkConnectionPool of(Address address, int poolSize)
    {
        Objects.requireNonNull(address, "address can not be null");
        return new NetworkConnectionPool(address, poolSize);
    }

    public NetworkConnection getOrCreateConnection(Address address, ProtocolVersion protocolVersion, NetworkListener networkListener)
    {
        if(!this.address.equals(address))
        {
            throw new CasualRuntimeException("Address mismatch, have: " + this.address + " got: " + address);
        }
        // create up to pool size # of connections
        // after that, randomly chose one - later one we can have some better heuristics for choosing which connection to return
        if(connections.size() == poolSize)
        {
            ReferenceCountedNetworkConnection connection = (connections.size() == 1) ? connections.get(0) : connections.get(getRandomNumber(0,poolSize - 1));
            connection.increment();
            return connection;
        }
        synchronized (connectionLock)
        {
            ReferenceCountedNetworkConnection connection = createNetworkConnection(address, protocolVersion, networkListener);
            connections.add(connection);
            connection.increment();
            return connection;
        }
    }

    @Override
    public void closed(ReferenceCountedNetworkConnection networkConnection)
    {
        synchronized (connectionLock)
        {
            connections.remove(networkConnection);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        NetworkConnectionPool that = (NetworkConnectionPool) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(address);
    }

    @Override
    public String toString()
    {
        return "NetworkConnectionPool{" +
                "address=" + address +
                ", connections=" + connections +
                ", poolSize=" + poolSize +
                ", connectionLock=" + connectionLock +
                '}';
    }

    private ReferenceCountedNetworkConnection createNetworkConnection(Address address, ProtocolVersion protocolVersion, NetworkListener networkListener)
    {
        Domain domain = ConfigurationService.getInstance().getConfiguration().getDomain();
        NettyConnectionInformation ci = NettyConnectionInformation.createBuilder().withAddress(new InetSocketAddress(address.getHostName(), address.getPort()))
                                                                  .withProtocolVersion(protocolVersion)
                                                                  .withDomainId(domain.getId())
                                                                  .withDomainName(domain.getName())
                                                                  .build();
        NetworkConnection networkConnection = NettyNetworkConnection.of(ci, networkListener);
        LOG.info(() -> "Created network connection: " + networkConnection);
        return ReferenceCountedNetworkConnection.of(networkConnection, this);
    }

    private static int getRandomNumber(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
}
