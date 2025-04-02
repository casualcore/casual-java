/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.store

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceBuilder
import spock.lang.Shared
import spock.lang.Specification

class ResourcesStoreTest extends Specification
{
    ResourcesStore instance

    @Shared
    String podName1 = "single-pod-resource-1"

    @Shared
    Pod pod1 = new PodBuilder(  )
            .withNewMetadata(  )
            .withName( podName1 )
            .endMetadata(  )
            .build(  )

    @Shared
    String podName2 = "single-pod-resource-2"

    @Shared
    Pod pod2 = new PodBuilder(  )
            .withNewMetadata(  )
            .withName( podName2 )
            .endMetadata(  )
            .build(  )

    @Shared
    String serviceName1 = "single-service-resource-1"

    @Shared
    Service service1 = new ServiceBuilder(  )
            .withNewMetadata(  )
            .withName( serviceName1 )
            .endMetadata(  )
            .build()

    @Shared
    String serviceName2 = "single-service-resource-2"

    @Shared
    Service service2 = new ServiceBuilder(  )
            .withNewMetadata(  )
            .withName( serviceName2 )
            .endMetadata(  )
            .build()

    def setup()
    {
        instance = new ResourcesStore()
    }

    def "Create, is empty."()
    {
        expect:
        instance.getPods() == [:]
        instance.getServices() == [:]
    }

    def "Retrieve non existent pod, throws ResourceNotFoundException"()
    {
        when:
        instance.getPod( "blah" )

        then:
        thrown ResourceNotFoundException
    }

    def "Remove non existent pod, throws ResourceNotFoundException"()
    {
        when:
        instance.removePod( "blah" )

        then:
        thrown ResourceNotFoundException
    }


    def "Add pods, retrieve, modify, retrieve, remove."()
    {
        when:
        instance.putPod( podName1, pod1 )

        then:
        instance.getPods() == [(podName1): pod1]
        instance.getPod( podName1 ) == pod1

        when:
        instance.putPod( podName2, pod2 )

        then:
        instance.getPods() == [(podName1): pod1, (podName2): pod2]
        instance.getPod( podName1 ) == pod1
        instance.getPod( podName2 ) == pod2

        when:
        instance.putPod( podName1, pod2 )
        instance.putPod( podName2, pod1 )

        then:
        instance.getPods() == [(podName1): pod2, (podName2): pod1]
        instance.getPod( podName1 ) == pod2
        instance.getPod( podName2 ) == pod1

        when:
        instance.removePod( podName1 )

        then:
        instance.getPods() == [(podName2): pod1 ]
    }

    def "Retrieve non existent service, throws ResourceNotFoundException"()
    {
        when:
        instance.getService( "blah" )

        then:
        thrown ResourceNotFoundException
    }

    def "Remove non existent Service, throws ResourceNotFoundException"()
    {
        when:
        instance.removeService( "blah" )

        then:
        thrown ResourceNotFoundException
    }

    def "Add services, retrieve, modify, retrieve, remove."()
    {
        when:
        instance.putService( serviceName1, service1 )

        then:
        instance.getServices() == [(serviceName1): service1]
        instance.getService( serviceName1 ) == service1

        when:
        instance.putService( serviceName2, service2 )

        then:
        instance.getServices() == [(serviceName1): service1, (serviceName2): service2]
        instance.getService( serviceName1 ) == service1
        instance.getService( serviceName2 ) == service2

        when:
        instance.putService( serviceName1, service2 )
        instance.putService( serviceName2, service1 )

        then:
        instance.getServices() == [(serviceName1): service2, (serviceName2): service1]
        instance.getService( serviceName1 ) == service2
        instance.getService( serviceName2 ) == service1

        when:
        instance.removeService( serviceName1 )

        then:
        instance.getServices() == [(serviceName2): service1 ]
    }

    def "Put all pods."()
    {
        given:
        Map<String, Pod> pods = [(podName1): pod1, (podName2): pod2 ]

        when:
        instance.putPods( pods )

        then:
        instance.getPods(  ) == pods
    }

    def "Put all services."()
    {
        given:
        Map<String, Service> services = [(serviceName1): service1,(serviceName2): service2]

        when:
        instance.putServices( services )

        then:
        instance.getServices() == services
    }
}
