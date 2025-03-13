/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import spock.lang.Specification

class TestKubeTest extends Specification
{
    TestKube instance

    def setup()
    {
        instance = TestKube.newBuilder().build()
    }

    def "Get client instance, default."()
    {
        expect:
        instance.getClient() != null
    }

    def "Get client instance, provided."()
    {
        given:
        KubernetesClient client = new KubernetesClientBuilder().build(  )

        when:
        instance = TestKube.newBuilder().client( client ).build()

        then:
        instance.getClient() == client
    }

    def "Get label, default."()
    {
        expect:
        instance.getLabel(  ) != null
    }

    def "Get label, provided."()
    {
        given:
        String label = "TestLabel"

        when:
        instance = TestKube.newBuilder().label( label ).build()

        then:
        instance.getLabel() == label
    }

    def "Create TestKube with a single pod resource."()
    {
        given:
        Pod pod = new PodBuilder(  )
                .withNewMetadata(  )
                    .withName( "single-pod-resource" )
                .endMetadata(  )
                .build(  )
        when:
        instance = TestKube.newBuilder().addPod( pod ).build()

        then:
        instance.getPods( ) == [pod]
    }
}
