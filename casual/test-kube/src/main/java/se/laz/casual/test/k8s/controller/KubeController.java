/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.controller;

import se.laz.casual.test.k8s.TestKube;
import se.laz.casual.test.k8s.connection.KubeConnection;
import se.laz.casual.test.k8s.exec.ExecResult;

import java.util.concurrent.CompletableFuture;

public class KubeController
{
    private final TestKube testKube;
    private final ProvisioningController provisioningController;
    private final ConnectionController connectionController;
    private final ExecController execController;
    private final LogController logController;

    public KubeController( TestKube testKube )
    {
        this.testKube = testKube;
        this.provisioningController = new ProvisioningController( testKube );
        this.connectionController = new ConnectionController( testKube );
        this.execController = new ExecController( testKube );
        this.logController = new LogController( testKube );
    }

    // Provisioning Controller

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

    // ConnectionController

    public KubeConnection getConnection( String resource, int targetPort )
    {
        return connectionController.getConnection( resource, targetPort );
    }

    public KubeConnection getPortForwardConnection( String resource, int port )
    {
        return connectionController.getPortForwardConnection( resource, port );
    }

    // Execution Controller

    public ExecResult executeCommand( String pod, String... command )
    {
        return this.execController.executeCommand( pod, command );
    }

    public CompletableFuture<ExecResult> executeCommandAsync( String pod, String... command )
    {
        return this.execController.executeCommandAsync( pod, command );
    }

    // Log Controller

    public String getLog( String pod )
    {
        return this.logController.getLog( pod );
    }

    public String getLogTail( String pod, int lines )
    {
        return this.logController.getLogTail( pod, lines );
    }

    public String getLogSince( String pod, String sinceTime )
    {
        return this.logController.getLogSince( pod, sinceTime );
    }
}
