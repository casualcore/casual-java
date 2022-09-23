package se.laz.casual.jca.pool;

import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.Address;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.outbound.NetworkListener;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class NetworkPoolHandler
{
    private static final NetworkPoolHandler instance = new NetworkPoolHandler();
    private final Map<Address, NetworkConnectionPool> pools = new ConcurrentHashMap<>();

    public static NetworkPoolHandler getInstance()
    {
        return instance;
    }

    public NetworkConnection getOrCreate(Address address, ProtocolVersion protocolVersion, NetworkListener listener, int poolSize)
    {
        // TODO:
        // check configuration for this address to get the pool size
        return pools.computeIfAbsent(address, key -> NetworkConnectionPool.of(key, poolSize)).getOrCreateConnection(address, protocolVersion,listener);
    }

    // used by jmx only
    public NetworkConnectionPool getPool(Address address)
    {
       return pools.get(address);
    }

    public Map<Address, NetworkConnectionPool> getPools()
    {
       return Collections.unmodifiableMap(pools);
    }

}
