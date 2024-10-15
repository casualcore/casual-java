/*
 * Copyright (c) 2022 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool;

import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.Address;
import se.laz.casual.jca.CasualResourceAdapterException;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.outbound.NettyConnectionInformation;
import se.laz.casual.network.outbound.NettyConnectionInformationCreator;
import se.laz.casual.network.outbound.NettyNetworkConnection;
import se.laz.casual.network.outbound.NetworkListener;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class NetworkConnectionPool implements ReferenceCountedNetworkCloseListener, NetworkListener
{
    private static final Logger LOG = Logger.getLogger(NetworkConnectionPool.class.getName());
    private final Address address;
    private final ConnectionContainer connections = ConnectionContainer.of();
    private final String poolName;
    private int poolSize;
    private final Object getOrCreateLock = new Object();
    private final NetworkConnectionCreator networkConnectionCreator;
    private final AtomicBoolean disconnected = new AtomicBoolean(false);

    private NetworkConnectionPool(String poolName, Address address, int poolSize, NetworkConnectionCreator networkConnectionCreator)
    {
        this.address = address;
        this.poolSize = poolSize;
        this.networkConnectionCreator = networkConnectionCreator;
        this.poolName = poolName;
    }

    public static NetworkConnectionPool of(String poolName, Address address, int poolSize)
    {
        return of(poolName, address, poolSize, null);
    }

    public static NetworkConnectionPool of(String poolName, Address address, int poolSize, NetworkConnectionCreator networkConnectionCreator)
    {
        Objects.requireNonNull(address, "poolName can not be null");
        Objects.requireNonNull(address, "address can not be null");
        networkConnectionCreator = null == networkConnectionCreator ? NetworkConnectionPool::createNetworkConnection : networkConnectionCreator;
        return new NetworkConnectionPool(poolName, address, poolSize, networkConnectionCreator);
    }

    public NetworkConnection getOrCreateConnection(Address address, ProtocolVersion protocolVersion, NetworkListener networkListener)
    {
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
            if (connections.size() == poolSize)
            {
                ReferenceCountedNetworkConnection connection = connections.get();
                connection.increment();
                connection.addListener(networkListener);
                return connection;
            }
            ReferenceCountedNetworkConnection connection = networkConnectionCreator.createNetworkConnection(address, protocolVersion, networkListener, this, this);
            connections.addConnection(connection);
            return connection;
        }
    }

    @Override
    public void closed(ReferenceCountedNetworkConnection networkConnection)
    {
        synchronized (getOrCreateLock)
        {
            connections.removeConnection(networkConnection);
            LOG.finest(() -> "removed: " + networkConnection + " from: " + this);
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
        return Objects.equals(address, that.address) && Objects.equals(poolName, that.poolName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(address, poolName);
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
                '}';
    }

    private static ReferenceCountedNetworkConnection createNetworkConnection(Address address, ProtocolVersion protocolVersion, NetworkListener networkListener, ReferenceCountedNetworkCloseListener referenceCountedNetworkCloseListener, NetworkListener ownListener)
    {
        NettyConnectionInformation ci = NettyConnectionInformationCreator.create(InetSocketAddress.createUnresolved(address.getHostName(), address.getPort()), protocolVersion);
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
