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
import se.laz.casual.test.k8s.KubeConnection
import se.laz.casual.test.k8s.TestKube
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ConnectivityIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = ConnectivityIntTest.class.getSimpleName(  )
    @Shared
    TestKube instance

    def setupSpec()
    {

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0

        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName( "wildfly-test" )
                .addToLabels( "app", "wildfly" )
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

//        Service service = new ServiceBuilder(  )
//                .withNewMetadata(  )
//                    .withName( "wildfly-service" )
//                    .addToLabels( "TestKube", id )
//                .endMetadata(  )
//                .withNewSpec(  )
//                    .addToSelector( "app","wildfly" )
//                    .addNewPort(  ).withPort( 9990 ).endPort(  )
//                .endSpec(  )
//                .build(  )

        instance = TestKube.newBuilder()
                .label( id )
                .addPod( pod )
                .build()

        instance.init(  )

        //client.services(  ).resource( service ).serverSideApply(  )

    }

    def cleanupSpec()
    {
        instance.destroy(  )

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0
    }

    def "Connection to pod, port 9990 using port forward."()
    {
        when:
        int status = 0
        String body = null

        try( KubeConnection connection = instance.getConnection( "wildfly-test", 9990 ) )
        {
            String host = connection.getHostName()
            int port = connection.getPort()

            HttpClient httpClient = HttpClient.newBuilder(  ).build(  )
            HttpRequest request = HttpRequest.newBuilder( )
                    .uri( URI.create( "http://" + host + ":" + port +"/" ) )
                    .GET( )
                    .build(  )

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            status = response.statusCode(  )
            body = response.body(  )
        }

        then:
        status == 302
        body == ""
    }

    def "Connect to pod, port 8080, using port forward."()
    {
        when:
        int status = 0
        String body = null

        try( KubeConnection connection = instance.getConnection( "wildfly-test", 8080 ) )
        {
            String host = connection.getHostName()
            int port = connection.getPort()

            HttpClient httpClient = HttpClient.newBuilder(  ).build(  )
            HttpRequest request = HttpRequest.newBuilder( )
                    .uri( URI.create( "http://" + host + ":" + port +"/" ) )
                    .GET( )
                    .build(  )

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            status = response.statusCode(  )
            body = response.body(  )
        }

        then:
        status == 200
        body != ""
        body.containsIgnoreCase( "wildfly" )
    }

    def "Connect to pod, port 8080, using port forward, not using try with resources."()
    {
        when:
        int status = 0
        String body = null

        KubeConnection connection = instance.getConnection( "wildfly-test", 8080 )

        String host = connection.getHostName()
        int port = connection.getPort()

        HttpClient httpClient = HttpClient.newBuilder(  ).build(  )
        HttpRequest request = HttpRequest.newBuilder( )
                .uri( URI.create( "http://" + host + ":" + port +"/" ) )
                .GET( )
                .build(  )

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        status = response.statusCode(  )
        body = response.body(  )


        then:
        status == 200
        body != ""
        body.containsIgnoreCase( "wildfly" )
    }
}
