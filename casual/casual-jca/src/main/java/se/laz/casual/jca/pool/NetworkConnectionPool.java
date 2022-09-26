package se.laz.casual.jca.pool;

import se.laz.casual.config.ConfigurationService;
import se.laz.casual.config.Domain;
import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.Address;
import se.laz.casual.jca.CasualResourceAdapterException;
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
    private final NetworkConnectionCreator networkConnectionCreator;

    private NetworkConnectionPool(Address address, int poolSize, NetworkConnectionCreator networkConnectionCreator)
    {
        this.address = address;
        this.poolSize = poolSize;
        this.networkConnectionCreator = networkConnectionCreator;
    }

    public static NetworkConnectionPool of(Address address, int poolSize)
    {
        return of(address, poolSize, null);
    }

    public static NetworkConnectionPool of(Address address, int poolSize, NetworkConnectionCreator networkConnectionCreator)
    {
        Objects.requireNonNull(address, "address can not be null");
        networkConnectionCreator = null == networkConnectionCreator ? NetworkConnectionPool::createNetworkConnection : networkConnectionCreator;
        return new NetworkConnectionPool(address, poolSize, networkConnectionCreator);
    }

    public NetworkConnection getOrCreateConnection(Address address, ProtocolVersion protocolVersion, NetworkListener networkListener)
    {
        if(!this.address.equals(address))
        {
            throw new CasualResourceAdapterException("Address mismatch, have: " + this.address + " got: " + address);
        }
        // create up to pool size # of connections
        // after that, randomly chose one - later on we can have some better heuristics for choosing which connection to return
        if(connections.size() == poolSize)
        {
            ReferenceCountedNetworkConnection connection = (connections.size() == 1) ? connections.get(0) : connections.get(getRandomNumber(0, poolSize));
            connection.increment();
            connection.addListener(networkListener);
            return connection;
        }
        synchronized (connectionLock)
        {
            ReferenceCountedNetworkConnection connection = networkConnectionCreator.createNetworkConnection(address, protocolVersion, networkListener, this);
            connections.add(connection);
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
                '}';
    }

    private static ReferenceCountedNetworkConnection createNetworkConnection(Address address, ProtocolVersion protocolVersion, NetworkListener networkListener, ReferenceCountedNetworkCloseListener referenceCountedNetworkCloseListener)
    {
        Domain domain = ConfigurationService.getInstance().getConfiguration().getDomain();
        NettyConnectionInformation ci = NettyConnectionInformation.createBuilder().withAddress(new InetSocketAddress(address.getHostName(), address.getPort()))
                                                                  .withProtocolVersion(protocolVersion)
                                                                  .withDomainId(domain.getId())
                                                                  .withDomainName(domain.getName())
                                                                  .build();
        NettyNetworkConnection networkConnection = NettyNetworkConnection.of(ci, networkListener);
        LOG.info(() -> "created network connection: " + networkConnection);
        return ReferenceCountedNetworkConnection.of(networkConnection, referenceCountedNetworkCloseListener);
    }

    // max - exclusive upper limit
    private static int getRandomNumber(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
}
