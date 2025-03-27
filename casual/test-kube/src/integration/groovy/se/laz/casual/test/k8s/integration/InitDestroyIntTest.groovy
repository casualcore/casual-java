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
import se.laz.casual.test.k8s.TestKube
import spock.lang.Shared
import spock.lang.Specification

class InitDestroyIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = InitDestroyIntTest.class.getSimpleName(  )

    def setupSpec()
    {

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0
    }

    def cleanupSpec()
    {
        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0
    }

    def "Create TestKube with a single pod resource."()
    {
        given:
        Pod pod = new PodBuilder(  )
                .withNewMetadata( )
                    .withName( "wildfly-test" )
                .endMetadata(  )
                .withNewSpec( )
                    .addNewContainer( )
                        .withName( "wildfly")
                        .withImage("quay.io/wildfly/wildfly:32.0.1.Final-jdk21"  )
                        .addNewPort().withContainerPort( 8080 ).endPort(  )
                    .endContainer(  )
                .endSpec(  )
                .build(  )

        TestKube instance = TestKube.newBuilder()
                .label( id )
                .addPod( pod )
                .build()

        when:
        instance.init()

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 1

        when:
        instance.destroy()

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
    }

    def "Create TestKube with a single pod resource async."()
    {
        given:
        Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName( "wildfly-test" )
                .endMetadata()
                .withNewSpec()
                    .addNewContainer()
                        .withName( "wildfly" )
                        .withImage( "quay.io/wildfly/wildfly:32.0.1.Final-jdk21" )
                        .addNewPort().withContainerPort( 8080 ).endPort()
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

        TestKube instance = TestKube.newBuilder()
                .label( id )
                .addPod( pod )
                .build()

        when:
        instance.getController().initAsync(  )

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 1

        when:
        instance.getController(  ).waitUntilReady(  )

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 1

        when:
        instance.getController().destroyAsync(  )

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 1

        when:
        instance.getController().waitUntilDestroyed(  )

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
    }
}
