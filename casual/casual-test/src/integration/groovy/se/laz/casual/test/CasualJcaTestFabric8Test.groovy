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
import io.fabric8.kubernetes.client.LocalPortForward
import io.fabric8.kubernetes.client.Watch
import io.fabric8.kubernetes.client.Watcher
import io.fabric8.kubernetes.client.WatcherException
import io.fabric8.kubernetes.client.utils.Serialization
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Ignore
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

    def "Create, wait, delete pod"()
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
                .addNewPort().withContainerPort( 9990 ).endPort(  )
                .withNewReadinessProbe(  )
                    .withNewTcpSocket(  )
                        .withNewPort()
                            .withValue( 8080 )
                        .endPort(  )
                    .endTcpSocket(  )
                .endReadinessProbe(  )
                .endContainer(  )
                .endSpec(  )
                .build(  )

        String podString = Serialization.asYaml( p )

        //final CountDownLatch readyLatch = new CountDownLatch( 1 )
        CountDownLatch deleteLatch = new CountDownLatch( 1 )

        Watch podWatch = client.pods().withName( "casual-jca-test" ).watch( new Watcher<Pod>(){

            @Override
            void eventReceived( Watcher.Action action, Pod resource )
            {
                System.out.println( action.toString(  ) + ":" + resource.getStatus(  ).toString(  ) )
                switch( action )
                {
                    case Watcher.Action.DELETED:
                        System.out.println( "Deleted" )
                        deleteLatch.countDown()
                }
            }

            @Override
            void onClose( WatcherException cause )
            {
                System.out.println( "Exception: " + cause.toString(  ) )
            }
        })

        when:
        Pod pod = client.resource( p ).serverSideApply(  )

        client.pods( ).withName( pod.getMetadata(  ).getName(  ) ).waitUntilReady( 2, TimeUnit.MINUTES )

        LocalPortForward portForward = client.pods( ).withName( pod.getMetadata(  ).getName(  ) ).portForward( 9990, 9990 )

        HttpClient httpClient = HttpClient.newBuilder(  ).build(  )
        HttpRequest request = HttpRequest.newBuilder( )
                .uri( URI.create( "http://localhost:9990/" ) )
                .GET( )
                .build(  )

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode(  )
        String body = response.body(  )

        then:
        status == 200
        body != ""


        cleanup:
        portForward.close(  )
        client.pods(  ).resource( p ).delete()
        deleteLatch.await( 2, TimeUnit.MINUTES)
        podWatch.close(  )
    }
}
