/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.controller;

import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import se.laz.casual.test.k8s.TestKube;
import se.laz.casual.test.k8s.TestKubeException;
import se.laz.casual.test.k8s.exec.ExecResult;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AlternateExecController
{
    private final TestKube testKube;

    public AlternateExecController( TestKube testKube )
    {
        this.testKube = testKube;
    }

    public ExecResult executeCommand( String pod, String... command )
    {
        return executeCommandAsync( pod, command ).join();
    }

    public ExecResult executeCommandUsingAsyncJoinUnwrapping( String pod, String... command )
    {
        try
        {
            return executeCommandAsync( pod, command ).join();
        }
        catch( CompletionException ce )
        {
            throw new TestKubeException( "Execution failed.", ce.getCause() );
        }
    }

    public CompletableFuture<ExecResult> executeCommandAsync( String pod, String... command )
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        PodResource p = testKube.getClient().pods().withName( pod );
        final ExecWatch watch = p.writingOutput( out ).writingError( out )
                .exec( command );

        return watch.exitCode().thenApplyAsync( ( exitCode ) -> {
            watch.close();
            return ExecResult.newBuilder()
                    .exitCode( exitCode )
                    .output( out.toString() )
                    .build();

        } );

    }

    public CompletableFuture<ExecResult> executeCommandAsyncUsingHandle( String pod, String... command )
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        PodResource p = testKube.getClient().pods().withName( pod );
        final ExecWatch watch = p.writingOutput( out ).writingError( out )
                .exec( command );

        return watch.exitCode().handle( ( exitCode, t ) -> {
            watch.close();
            if( t != null )
            {
                throw new TestKubeException( "Exec failed.", t );
            }
            return ExecResult.newBuilder()
                    .exitCode( exitCode )
                    .output( out.toString() )
                    .build();

        } );
    }
}
