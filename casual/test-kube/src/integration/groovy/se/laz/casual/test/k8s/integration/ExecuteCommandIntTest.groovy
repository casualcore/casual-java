/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.integration

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.dsl.ExecWatch
import se.laz.casual.test.k8s.TestKube
import spock.lang.Shared
import spock.lang.Specification

class ExecuteCommandIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = ExecuteCommandIntTest.class.getSimpleName(  )
    @Shared
    TestKube instance

    def setupSpec()
    {

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0

        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName( "wildfly-test" )
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName( "wildfly" )
                .withImage( "quay.io/wildfly/wildfly:32.0.1.Final-jdk21" )
                .addNewPort().withContainerPort( 8080 ).endPort()
                .addNewPort().withContainerPort( 9990 ).endPort(  )
                .withNewReadinessProbe()
                .withNewTcpSocket()
                .withNewPort()
                .withValue( 8080 )
                .endPort()
                .endTcpSocket()
                .endReadinessProbe()
                .endContainer()
                .endSpec()
                .build()

        instance = TestKube.newBuilder()
                .label( id )
                .addPod( pod )
                .build()

        instance.init(  )

    }

    def cleanupSpec()
    {
        instance.destroy(  )

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0
    }

    def "Execute a command on a pod."()
    {
        given:
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when:
        ExecWatch watch = client.pods().withName( "wildfly-test" )
                .writingOutput( out )
                .exec( "sh", "-c", "ls -l" )

        int exitCode = watch.exitCode(  ).join(  )
        String actual = out.toString()

        then:
        exitCode == 0
        actual != ""
        actual.containsIgnoreCase( "wildfly" )

        cleanup:
        watch.close(  )
    }
}
