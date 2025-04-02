/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.connection;

/**
 * Service connection.
 */
public class ServiceConnection implements KubeConnection
{
    private final String hostname;
    private final int port;

    public ServiceConnection( String hostname, int port )
    {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public KubeConnectionType getType()
    {
        return KubeConnectionType.SERVICE;
    }

    @Override
    public String getHostName()
    {
        return this.hostname;
    }

    @Override
    public int getPort()
    {
        return this.port;
    }

    @Override
    public void close()
    {
    }
}
