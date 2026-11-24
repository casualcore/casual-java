/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.logging.Logger;

// singleton is intentional
@SuppressWarnings("java:S6548")
public class NetworkPoolHandler
{
    private static final Logger log = Logger.getLogger(NetworkPoolHandler.class.getName());
    private static final NetworkPoolHandler instance = new NetworkPoolHandler();
    private final Map<String, NetworkConnectionPool> pools = new ConcurrentHashMap<>();

    private NetworkPoolHandler()
    {}

    // Exposed internal representation is expected.
    @SuppressFBWarnings("MS_EXPOSE_REP")
    public static NetworkPoolHandler getInstance()
    {
        return instance;
    }

    /**
     * Returns an active network connection from the named normal outbound pool, creating the pool
     * when necessary.
     *
     * @param poolName the unique pool name
     * @param address the target outbound address
     * @param listener the listener to notify when the connection closes
     * @param poolSize the configured network connection pool size
     * @return an active network connection
     */
    public NetworkConnection getOrCreate(String poolName, Address address, NetworkListener listener, int poolSize)
    {
        try
        {
            NetworkConnectionPool pool = pools.computeIfAbsent(poolName, key -> NetworkConnectionPool.of(key, address, poolSize));
            return pool.getOrCreateConnection(address, listener);
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
     * Returns an active reverse outbound connection pinned to the specified remote domain.
     *
     * @param poolName the unique reverse pool name
     * @param listener the listener to notify when the connection closes
     * @param domainId the remote domain ID
     * @return an active network connection for the remote domain
     * @throws NullPointerException if {@code poolName}, {@code listener}, or {@code domainId} is {@code null}
     * @throws CasualConnectionException if no reverse pool is registered for {@code poolName} or
     *         no connection is available for {@code domainId}
     */
    public NetworkConnection getReverse(String poolName, NetworkListener listener, DomainId domainId)
    {
        Objects.requireNonNull(poolName, "poolName must not be null");
        Objects.requireNonNull(listener, "listener must not be null");
        Objects.requireNonNull(domainId, "domainId must not be null");
        NetworkConnectionPool pool = pools.get(poolName);
        if (pool == null)
        {
            throw new CasualConnectionException("no reverse outbound pool registered for: " + poolName);
        }
        return pool.getReverseConnection(listener, domainId);
    }

    /**
     * Returns the reverse pool with the specified name, creating it when necessary.
     *
     * <p>Reverse pools are registered when the reverse outbound listeners start so that they exist
     * before any connection factory can look them up.
     *
     * @param poolName the unique reverse pool name
     * @return the existing or newly created reverse pool
     * @throws NullPointerException if {@code poolName} is {@code null}
     */
    public NetworkConnectionPool getOrCreateReversePool(String poolName)
    {
        return pools.computeIfAbsent(poolName, NetworkConnectionPool::ofReverse);
    }

    /**
     * Adds an established reverse outbound connection to the pool with the specified name.
     *
     * @param poolName the unique reverse pool name
     * @param connection the established connection to add
     * @throws NullPointerException if {@code poolName}, {@code connection}, or the connection's
     *         domain ID is {@code null}
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
