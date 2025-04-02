/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.controller;

import se.laz.casual.test.k8s.TestKube;
import se.laz.casual.test.k8s.connection.KubeConnection;

public class KubeController
{
    private final TestKube testKube;
    private final ProvisioningController provisioningController;
    private final ConnectionController connectionController;

    public KubeController( TestKube testKube )
    {
        this.testKube = testKube;
        this.provisioningController = new ProvisioningController( testKube );
        this.connectionController = new ConnectionController( testKube );
    }

    public void init()
    {
        provisioningController.init();
    }

    public void initAsync()
    {
        provisioningController.initAsync();
    }

    public void waitUntilReady()
    {
        provisioningController.waitUntilReady();
    }

    public void destroy()
    {
        provisioningController.destroy();
    }

    public void destroyAsync()
    {
        provisioningController.destroyAsync();
    }

    public void waitUntilDestroyed()
    {
        provisioningController.waitUntilDestroyed();
    }

    public KubeConnection getConnection( String resource, int targetPort )
    {
        return connectionController.getConnection( resource, targetPort );
    }

    public KubeConnection getPortForwardConnection( String resource, int port )
    {
        return connectionController.getPortForwardConnection( resource, port );
    }
}
