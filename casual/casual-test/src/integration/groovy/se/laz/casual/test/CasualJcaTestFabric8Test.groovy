/*
 * Copyright (c) 2024 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test


import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class CasualJcaTestFabric8Test extends Specification
{

    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build(  )

    def setupSpec()
    {
//        VersionInfo info = client.getKubernetesVersion(  )
//        printGetMethods( info )
//
//        Config config = client.getConfiguration(  )
//        printGetMethods( config )
    }

    void printGetMethods( Object o )
    {
        for( Method m : o.getClass(  ).getMethods(  ) )
        {
            if( m.getName(  ).startsWith( "get" ) && m.getParameterCount(  ) == 0 )
            {
                System.out.println( m.getName() + " : " + m.invoke( o ) )
            }
        }
    }

    def "Create pod"()
    {
        given:
        Pod p = new PodBuilder(  )
                .withNewMetadata( )
                    .withName( "casual-jca-test" )
                .endMetadata(  )
                .withNewSpec( )
                    .addNewContainer( )
                        .withName( "casual-jca")
                        .withImage("192.168.68.130:5000/casual-java:3.2.50-SNAPSHOT"  )
                        .addNewPort().withContainerPort( 8080 ).endPort(  )
                    .endContainer(  )
                .endSpec(  )
                .build(  )

        when:
        client.pods(  ).resource( p ).create()

        then:
        noExceptionThrown(  )
    }

    def "Delete pod"()
    {
        given:
        Pod p = new PodBuilder(  )
                .withNewMetadata( )
                .withName( "casual-jca-test" )
                .endMetadata(  )
                .build(  )

        when:
        client.pods(  ).resource( p ).delete()

        then:
        noExceptionThrown(  )
    }
}
