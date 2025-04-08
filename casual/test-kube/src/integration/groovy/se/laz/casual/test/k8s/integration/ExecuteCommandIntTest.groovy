/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.integration

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import se.laz.casual.test.k8s.TestKube
import se.laz.casual.test.k8s.exec.ExecResult
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class ExecuteCommandIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = ExecuteCommandIntTest.class.getSimpleName(  )
    @Shared
    TestKube instance
    @Shared
    String podName = NginxResources.SIMPLE_NGINX_POD_NAME
    @Shared
    Pod pod = NginxResources.SIMPLE_NGINX_POD

    def setupSpec()
    {

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0

        instance = TestKube.newBuilder()
                .label( id )
                .addPod( podName, pod )
                .build()

        instance.init(  )

    }

    def cleanupSpec()
    {
        instance.destroy(  )

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0
    }

    def "Execute a command on a pod, sync."()
    {
        given:
        String out = "hello exec world."
        String[] command = ["sh", "-c", "echo -n "+ out ]
        ExecResult expected = ExecResult.newBuilder().output( out ).build()

        when:
        ExecResult actual = instance.getController().executeCommand( podName, command )

        then:
        actual == expected
    }

    def "Execute a command on a pod, async."()
    {
        given:
        String out = "hello exec world."
        String[] command = ["sh", "-c", "sleep 0.01; echo -n " + out ]
        ExecResult expected = ExecResult.newBuilder().output( out ).build()

        when:
        CompletableFuture<ExecResult> execFuture = instance.getController().executeCommandAsync( podName, command )
        ExecResult actual = execFuture.join(  )

        then:
        actual == expected
    }

    def "Execute a command on a pod, errors."()
    {
        given:
        String[] command = ["sh", "-c", "blah" ]

        when:
        ExecResult actual = instance.getController().executeCommand( podName, command )

        then:
        actual.getExitCode(  ) == 127
        actual.getOutput(  ).contains( "blah: not found" )
    }

    def "Execute a command on a pod, returns correct exitCode."()
    {
        given:
        String[] command = ["sh", "-c", "exit " + exitCode ]
        ExecResult expected = ExecResult.newBuilder().exitCode( exitCode ).build(  )

        when:
        ExecResult actual = instance.getController().executeCommand( podName, command )

        then:
        actual == expected

        where:
        exitCode << [
                1, 2, 3, 4, 0
        ]
    }
}
