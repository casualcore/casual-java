/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.connection;

import io.fabric8.kubernetes.client.LocalPortForward;

import java.io.IOException;

/**
 * Port forwarded connection.
 */
public class PortForwardedConnection implements KubeConnection
{
    private LocalPortForward localPortForward;

    public PortForwardedConnection( LocalPortForward localPortForward )
    {
        this.localPortForward = localPortForward;
    }

    @Override
    public KubeConnectionType getType()
    {
        return KubeConnectionType.PORT_FORWARDED;
    }

    @Override
    public String getHostName()
    {
        return localPortForward.getLocalAddress().getHostName();
    }

    @Override
    public int getPort()
    {
        return localPortForward.getLocalPort();
    }

    @Override
    public void close()
    {
        try
        {
            localPortForward.close();
        }
        catch ( IOException e )
        {
            throw new ConnectionCloseException( "Close failed.", e );
        }
    }
}
