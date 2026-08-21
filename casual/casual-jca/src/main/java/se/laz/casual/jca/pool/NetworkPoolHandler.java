/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool;

import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.Address;
import se.laz.casual.jca.DomainId;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.outbound.NettyNetworkConnection;
import se.laz.casual.network.outbound.NetworkListener;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

// singleton is intentional
@SuppressWarnings("java:S6548")
public class NetworkPoolHandler
{
    private static final Logger log = Logger.getLogger(NetworkPoolHandler.class.getName());
    private static final NetworkPoolHandler instance = new NetworkPoolHandler();
    private final Map<String, NetworkConnectionPool> pools = new ConcurrentHashMap<>();

    private NetworkPoolHandler()
    {
    }

    public static NetworkPoolHandler getInstance()
    {
        return instance;
    }

    public NetworkConnection getOrCreate(String poolName, Address address, NetworkListener listener, int poolSize)
    {
        return getOrCreate(poolName, address, poolSize, pool -> pool.getOrCreateConnection(address, listener));
    }

    /**
     * Get or create a connection pinned to a specific remote domain - see
     * {@link NetworkConnectionPool#getOrCreateConnection(Address, NetworkListener, DomainId)}.
     */
    public NetworkConnection getOrCreate(String poolName, Address address, NetworkListener listener, int poolSize, DomainId domainId)
    {
        Objects.requireNonNull(domainId, "domainId can not be null");
        return getOrCreate(poolName, address, poolSize, pool -> pool.getOrCreateConnection(address, listener, domainId));
    }

    private NetworkConnection getOrCreate(String poolName, Address address, int poolSize, Function<NetworkConnectionPool, NetworkConnection> connectionGetter)
    {
        try
        {
            return connectionGetter.apply(pools.computeIfAbsent(poolName, key -> NetworkConnectionPool.of(key, address, poolSize)));
        }
        catch(CasualConnectionException e)
        {
            log.finest(() -> "connection failure for: " + address);
            NetworkConnectionPool pool = pools.get(poolName);
            if(null != pool && !pool.isReverse())
            {
                // reverse pools are kept, they are refilled as the EIS(s) reconnects
                log.finest(() -> "removing pool: " + poolName);
                pools.remove(poolName, pool);
            }
            throw e;
        }
    }

    /**
     * Get or create the reverse pool with the given name.
     * Reverse pools are registered when the reverse outbound listeners start so that they exist
     * before any connection factory can look them up.
     */
    public NetworkConnectionPool getOrCreateReversePool(String poolName)
    {
        return pools.computeIfAbsent(poolName, NetworkConnectionPool::ofReverse);
    }

    /**
     * Add an established reverse outbound connection to the pool with the given name.
     */
    public void addReverseConnection(String poolName, NettyNetworkConnection connection)
    {
        getOrCreateReversePool(poolName).addConnectionForReversePool(connection);
    }

    // used by jmx only
    public NetworkConnectionPool getPool(String poolName)
    {
       return pools.get(poolName);
    }

    public Map<String, NetworkConnectionPool> getPools()
    {
       return Collections.unmodifiableMap(pools);
    }

}
