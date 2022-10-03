/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool;

import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.Address;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.network.outbound.NetworkListener;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class NetworkPoolHandler
{
    private static final Logger log = Logger.getLogger(NetworkPoolHandler.class.getName());
    private static final NetworkPoolHandler instance = new NetworkPoolHandler();
    private final Map<String, NetworkConnectionPool> pools = new ConcurrentHashMap<>();

    public static NetworkPoolHandler getInstance()
    {
        return instance;
    }

    public NetworkConnection getOrCreate(String poolName, Address address, ProtocolVersion protocolVersion, NetworkListener listener, int poolSize)
    {
        try
        {
            return pools.computeIfAbsent(poolName, key -> NetworkConnectionPool.of(key, address, poolSize)).getOrCreateConnection(address, protocolVersion, listener);
        }
        catch(CasualConnectionException e)
        {
            log.finest(() -> "connection failure for: " + address);
            pools.remove(poolName);
            throw e;
        }
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
