/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool;

import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.Address;
import se.laz.casual.jca.CasualResourceAdapterException;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.outbound.NettyConnectionInformation;
import se.laz.casual.network.outbound.NettyConnectionInformationCreator;
import se.laz.casual.network.outbound.NettyNetworkConnection;
import se.laz.casual.network.outbound.NetworkListener;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class NetworkConnectionPool implements ReferenceCountedNetworkCloseListener, NetworkListener
{
    private static final Logger LOG = Logger.getLogger(NetworkConnectionPool.class.getName());
    // for reverse pools the address is unused, connections are established by the EIS
    private static final Address REVERSE_ADDRESS = Address.of("reverse", 0);
    private final Address address;
    // for reverse pools: each established connection acts, to the user, as if it was its own
    // configured pool - abstracted as one pool where a connection is chosen at random per managed connection
    private final ConnectionContainer connections = ConnectionContainer.of();
    private final String poolName;
    private int poolSize;
    private final Object getOrCreateLock = new Object();
    private final NetworkConnectionCreator networkConnectionCreator;
    private final AtomicBoolean disconnected = new AtomicBoolean(false);
    private final boolean reverse;

    private NetworkConnectionPool(String poolName, Address address, int poolSize, NetworkConnectionCreator networkConnectionCreator, boolean reverse)
    {
        this.address = address;
        this.poolSize = poolSize;
        this.networkConnectionCreator = networkConnectionCreator;
        this.poolName = poolName;
        this.reverse = reverse;
    }

    public static NetworkConnectionPool of(String poolName, Address address, int poolSize)
    {
        return of(poolName, address, poolSize, null);
    }

    public static NetworkConnectionPool of(String poolName, Address address, int poolSize, NetworkConnectionCreator networkConnectionCreator)
    {
        Objects.requireNonNull(poolName, "poolName can not be null");
        Objects.requireNonNull(address, "address can not be null");
        networkConnectionCreator = null == networkConnectionCreator ? NetworkConnectionPool::createNetworkConnection : networkConnectionCreator;
        return new NetworkConnectionPool(poolName, address, poolSize, networkConnectionCreator, false);
    }

    /**
     * Create a reverse pool.
     * A reverse pool never establishes any connections itself, it is fed established connections
     * via {@link #addConnectionForReversePool(NettyNetworkConnection)} as the EIS connects to a reverse outbound listener.
     */
    public static NetworkConnectionPool ofReverse(String poolName)
    {
        Objects.requireNonNull(poolName, "poolName can not be null");
        return new NetworkConnectionPool(poolName, REVERSE_ADDRESS, 0, NetworkConnectionPool::createNetworkConnection, true);
    }

    public boolean isReverse()
    {
        return reverse;
    }

    /**
     * Get a connection pinned to a specific remote domain.
     * The pin is only honored by reverse pools - it selects a connection towards that specific
     * connected instance, failing if the instance is gone. Non reverse pools ignore it since
     * all their connections go towards one and the same system anyway.
     */
    public NetworkConnection getOrCreateConnection(Address address, NetworkListener networkListener, DomainId domainId)
    {
        Objects.requireNonNull(domainId, "domainId can not be null");
        if(reverse)
        {
            return getReverseConnection(networkListener, domainId);
        }
        return getOrCreateConnection(address, networkListener);
    }

    public NetworkConnection getOrCreateConnection(Address address, NetworkListener networkListener)
    {
        if(reverse)
        {
            return getReverseConnection(networkListener);
        }
        if(!this.address.equals(address))
        {
            throw new CasualResourceAdapterException("Address mismatch, have: " + this.address + " got: " + address + " for pool with name: " + poolName);
        }
        if(disconnected.get())
        {
            throw new CasualConnectionException("disconnected");
        }
        synchronized (getOrCreateLock)
        {
            // create up to pool size # of connections
            // after that, randomly choose one - later on we can have some better heuristics for choosing which connection to return
            while (connections.size() == poolSize)
            {
                ReferenceCountedNetworkConnection connection = connections.get();
                if(connection.tryIncrement())
                {
                    connection.addListener(networkListener);
                    return connection;
                }
                // the last user just released it, its close notification is pending - drop it and create a replacement
                connections.removeConnection(connection);
            }
            ReferenceCountedNetworkConnection connection = networkConnectionCreator.createNetworkConnection(address, networkListener, this, this);
            connections.addConnection(connection);
            return connection;
        }
    }

    private NetworkConnection getReverseConnection(NetworkListener networkListener)
    {
        synchronized (getOrCreateLock)
        {
            for(;;)
            {
                ReferenceCountedNetworkConnection connection = anyReverseConnection().orElseThrow(() -> noReverseConnection(""));
                if(connection.tryIncrement())
                {
                    connection.addListener(networkListener);
                    return connection;
                }
                // the last user just released it, its close notification is pending - drop it
                connections.removeConnection(connection);
            }
        }
    }

    private NetworkConnection getReverseConnection(NetworkListener networkListener, DomainId domainId)
    {
        synchronized (getOrCreateLock)
        {
            for(;;)
            {
                ReferenceCountedNetworkConnection connection = connections.get(domainId).orElseThrow(() -> noReverseConnection(" towards domain: " + domainId));
                if(connection.tryIncrement())
                {
                    connection.addListener(networkListener);
                    return connection;
                }
                // the last user just released it, its close notification is pending - drop it
                connections.removeConnection(connection);
            }
        }
    }

    // only ever call while holding getOrCreateLock
    private Optional<ReferenceCountedNetworkConnection> anyReverseConnection()
    {
        return connections.size() == 0 ? Optional.empty() : Optional.of(connections.get());
    }

    private CasualConnectionException noReverseConnection(String detail)
    {
        return new CasualConnectionException("no reverse outbound connection available for pool: " + poolName + detail);
    }

    /**
     * The remote domain ids currently backing this pool.
     * For a reverse pool: the currently connected instances, one entry per instance no matter
     * how many connections each of them has established.
     */
    public List<DomainId> getPoolDomainIds()
    {
        synchronized (getOrCreateLock)
        {
            return connections.getDomainIds();
        }
    }

    /**
     * Add an established connection, reverse pools only.
     * Each established connection acts, to the user, as if it was its own configured pool towards
     * the connected instance - one is chosen at random per managed connection ( per domain id).
     *
     * The pool holds the initial reference so that managed connection churn never closes the
     * physical connection - a normal outbound pool is always configured to never be exhausted
     * (initial = min = max, no scaling) so its connections only ever close on network error,
     * and reverse connections behave the same: they live until the EIS closes them or the
     * resource adapter is deactivated.
     */
    public void addConnectionForReversePool(NettyNetworkConnection networkConnection)
    {
        if(!reverse)
        {
            throw new CasualResourceAdapterException("addConnection is only allowed for reverse pools, pool: " + poolName);
        }
        Objects.requireNonNull(networkConnection, "networkConnection can not be null");
        DomainId domainId = networkConnection.getDomainId();
        Objects.requireNonNull(domainId, "domainId can not be null");
        synchronized (getOrCreateLock)
        {
            ReferenceCountedNetworkConnection connection = ReferenceCountedNetworkConnection.of(networkConnection, this);
            networkConnection.addListener(exception -> closed(connection));
            connections.addConnection(connection);
            LOG.finest(() -> "added reverse outbound connection from domain: " + domainId + " to pool: " + poolName);
        }
    }

    @Override
    public void closed(ReferenceCountedNetworkConnection networkConnection)
    {
        synchronized (getOrCreateLock)
        {
            if(reverse)
            {
                // some EIS is failing
                // since we start at count 1 not 0 for reverse connections
                // this is for readability and testing, the action itself is idempotent - the network connection is already gone since we ended up here
                // each managed connection that was active when the network error occurred are closed by the appserver via managed connection destroy ->
                // ref counted connection close
                networkConnection.close();
            }
            connections.removeConnection(networkConnection);
            LOG.finest(() -> "removed( reverse=" + reverse + " ) : " + networkConnection + " from: " + this);
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
        return reverse == that.reverse && Objects.equals(address, that.address) && Objects.equals(poolName, that.poolName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(address, poolName, reverse);
    }

    @Override
    public String toString()
    {
        return "NetworkConnectionPool{" +
                "address=" + address +
                ", connections=" + connections +
                ", poolName='" + poolName + '\'' +
                ", poolSize=" + poolSize +
                ", disconnected=" + disconnected +
                ", reverse=" + reverse +
                '}';
    }

    private static ReferenceCountedNetworkConnection createNetworkConnection(Address address, NetworkListener networkListener, ReferenceCountedNetworkCloseListener referenceCountedNetworkCloseListener, NetworkListener ownListener)
    {
        NettyConnectionInformation ci = NettyConnectionInformationCreator.create(InetSocketAddress.createUnresolved(address.getHostName(), address.getPort()));
        NetworkConnection networkConnection = NettyNetworkConnection.of(ci, ownListener);
        if (networkConnection instanceof NettyNetworkConnection impl)
        {
            impl.addListener(networkListener);
            LOG.finest(() -> "created network connection: " + networkConnection);
            return ReferenceCountedNetworkConnection.of(impl, referenceCountedNetworkCloseListener);
        }
        throw new CasualResourceAdapterException("Wrong implementation for NetworkConnection, was expecting NettyNetworkConnection but got: " + networkConnection.getClass());
    }

    @Override
    public void disconnected(Exception reason)
    {
        disconnected.set(true);
    }
}
