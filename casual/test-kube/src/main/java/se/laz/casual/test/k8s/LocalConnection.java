/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s;

import io.fabric8.kubernetes.client.LocalPortForward;

import java.io.IOException;

public class LocalConnection implements KubeConnection
{
    private LocalPortForward localPortForward;

    public LocalConnection( LocalPortForward localPortForward )
    {
        this.localPortForward = localPortForward;
    }

    @Override
    public KubeConnectionType getType()
    {
        return KubeConnectionType.LOCAL_PORT_FORWARD;
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
            throw new RuntimeException( "Close failed.", e );
        }
    }
}
