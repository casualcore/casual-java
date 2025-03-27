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

class LoggingIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = LoggingIntTest.class.getSimpleName(  )
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

    def "Retrieve log from a pod."()
    {
        when:
        String log = client.pods().withName( "wildfly-test" )
                .getLog()

        then:
        log != ""
        log.containsIgnoreCase( "wildfly" )
    }

    def "Retrieve log from a pod, tailing."()
    {
        when:
        String log = client.pods().withName( "wildfly-test" )
                .tailingLines( 10 )
                .getLog()

        then:
        log != ""
        log.containsIgnoreCase( "wildfly" )
    }
}
