package se.laz.casual.jca.pool;

import se.laz.casual.internal.network.NetworkConnection;
import se.laz.casual.jca.Address;
import se.laz.casual.network.ProtocolVersion;
import se.laz.casual.network.outbound.NetworkListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PoolHandler
{
    private static final Logger log = Logger.getLogger(PoolHandler.class.getName());
    private static final PoolHandler instance = new PoolHandler();
    private static final int POOL_SIZE = 2;
    private final Map<Address, NetworkConnectionPool> pools = new ConcurrentHashMap<>();

    public static PoolHandler getInstance()
    {
        return instance;
    }

    public NetworkConnection getOrCreate(Address address, ProtocolVersion protocolVersion, NetworkListener listener)
    {
        // TODO:
        // check configuration for this address to get the pool size
        return pools.computeIfAbsent(address, key -> NetworkConnectionPool.of(key, POOL_SIZE)).getOrCreateConnection(address, protocolVersion,listener);
    }

}
