/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import spock.lang.Shared
import spock.lang.Specification

class TestKubeTest extends Specification
{
    @Shared
    String podName = "single-pod-resource"

    @Shared
    Pod pod = new PodBuilder(  )
            .withNewMetadata(  )
            .withName( podName )
            .endMetadata(  )
            .build(  )

    @Shared
    String serviceName = "single-service-resource"

    @Shared
    Service service = new ServiceBuilder(  )
            .withNewMetadata(  )
            .withName( serviceName )
            .endMetadata(  )
            .build()

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
        when:
        instance = TestKube.newBuilder().addPod( podName, pod ).build()

        then:
        instance.getPods( ) == [(podName): pod]
    }

    def "Create TestKube with a single pod and a single service."()
    {
        when:
        instance = TestKube.newBuilder(  )
                .addPod( podName, pod )
                .addService( serviceName, service ).build()

        then:
        instance.getPods(  ) == [(podName): pod]
        instance.getServices() == [(serviceName):service]
    }

    def "Create TestKube with a single pod, then update pod."()
    {
        given:
        instance = TestKube.newBuilder(  ).addPod( podName, pod ).build()
    }
}
