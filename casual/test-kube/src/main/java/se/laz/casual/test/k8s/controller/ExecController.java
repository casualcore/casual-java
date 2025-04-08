/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import se.laz.casual.test.k8s.TestKube;
import se.laz.casual.test.k8s.exec.ExecResult;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;

public class ExecController
{
    private final TestKube testKube;

    public ExecController( TestKube testKube )
    {
        this.testKube = testKube;
    }

    public ExecResult executeCommand( String pod, String... command )
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PodResource podResource = null;

        if( this.testKube.getResourcesStore().getPods().containsKey( pod ) )
        {
            Pod p = this.testKube.getResourcesStore().getPod( pod );
            podResource = this.testKube.getClient().pods().resource( p );
        }

        if( podResource == null )
        {
            podResource = testKube.getClient().pods().withName( pod );
        }

        try( ExecWatch watch = podResource.writingOutput( out ).writingError( out )
                .exec( command ) )
        {
            Integer exitCode = watch.exitCode().join();
            return ExecResult.newBuilder()
                    .exitCode( exitCode )
                    .output( out.toString() )
                    .build();
        }
    }

    public CompletableFuture<ExecResult> executeCommandAsync( String pod, String... command )
    {
        return CompletableFuture.supplyAsync( ()-> executeCommand( pod, command ) );
    }
}
