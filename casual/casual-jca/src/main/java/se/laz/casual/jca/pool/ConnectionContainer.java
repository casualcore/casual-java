/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ConnectionContainer
{
    private final List<ReferenceCountedNetworkConnection> connections = new ArrayList<>();
    private final Object lock = new Object();


    public static ConnectionContainer of()
    {
        return new ConnectionContainer();
    }

    public int size()
    {
        synchronized (lock)
        {
            return connections.size();
        }
    }

    public void addConnection(ReferenceCountedNetworkConnection connection)
    {
        synchronized (lock)
        {
            connections.add(connection);
        }
    }

    public void removeConnection(ReferenceCountedNetworkConnection connection)
    {
        synchronized (lock)
        {
            connections.remove(connection);
        }
    }

    public ReferenceCountedNetworkConnection get()
    {
        synchronized (lock)
        {
            if(connections.size() == 1)
            {
                return connections.get(0);
            }
            return connections.get(getRandomNumber(0, connections.size()));
        }
    }

    // pseudorandom is good enough here
    @SuppressWarnings("java:S2245")
    // max - exclusive upper limit
    private static int getRandomNumber(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

}
