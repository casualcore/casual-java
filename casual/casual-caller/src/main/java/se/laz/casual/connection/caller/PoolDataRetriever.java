/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.connection.caller.entities.ConnectionFactoryEntry;
import se.laz.casual.connection.caller.entities.Pool;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.CasualConnectionListener;

import javax.resource.ResourceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PoolDataRetriever
{
    public List<Pool> get(List<ConnectionFactoryEntry> connectionFactoryEntries)
    {
        return get(connectionFactoryEntries, null);
    }

    public List<Pool> get(List<ConnectionFactoryEntry> connectionFactoryEntries, CasualConnectionListener connectionListener)
    {
        List<Pool> pools = new ArrayList<>();
        for(ConnectionFactoryEntry connectionFactoryEntry : connectionFactoryEntries)
        {
            get(connectionFactoryEntry, connectionListener).ifPresent(pools::add);
        }
        return pools;
    }

    private Optional<Pool> get(ConnectionFactoryEntry connectionFactoryEntry, CasualConnectionListener connectionListener)
    {
        try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection())
        {
            if(null != connectionListener)
            {
                connection.addConnectionListener(connectionListener);
            }
            return Optional.of(Pool.of(connectionFactoryEntry, connection.getPoolDomainIds()));
        }
        catch (ResourceException e)
        {
            // NOP
            // we ignore this since it may be that the pool is currently unavailable
        }
        return Optional.empty();
    }
}

