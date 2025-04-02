/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.integration


import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import se.laz.casual.test.k8s.TestKube
import se.laz.casual.test.k8s.connection.KubeConnection
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class NginxConnectivityIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = NginxConnectivityIntTest.class.getSimpleName(  )
    @Shared
    TestKube instance
    @Shared
    String podName = NginxResources.SIMPLE_NGINX_POD_NAME
    @Shared
    String serviceName = NginxResources.SIMPLE_NGINX_SERVICE_NAME

    def setupSpec()
    {

        assert client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
        assert client.services(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0

        instance = TestKube.newBuilder()
                .label( id )
                .addPod( NginxResources.SIMPLE_NGINX_POD_NAME, NginxResources.SIMPLE_NGINX_POD )
                .addService( NginxResources.SIMPLE_NGINX_SERVICE_NAME, NginxResources.SIMPLE_NGINX_SERVICE )
                .addService( NginxResources.EXTERNAL_NGINX_SERVICE_NAME, NginxResources.EXTERNAL_NGINX_SERVICE )
                .build()

        instance.init(  )

    }

    def cleanupSpec()
    {
        instance.destroy(  )

        assert client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
        assert client.services(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
    }

    def "Connect to pod, port 80, using port forward."()
    {
        given:
        int status
        String body

        when:
        try( KubeConnection connection = instance.getPortForwardConnection( podName, 80 ) )
        {
            HttpResponse<String> response = httpGet( connection )
            status = response.statusCode(  )
            body = response.body()
        }

        then:
        status == 200
        body != ""
        body.containsIgnoreCase( "nginx" )
    }

    def "Connect to pod, port 80, using port forward, not using try with resources."()
    {
        when:
        KubeConnection connection = instance.getPortForwardConnection( podName, 80 )

        HttpResponse<String> response = httpGet( connection )
        int status = response.statusCode(  )
        String body = response.body()

        then:
        status == 200
        body != ""
        body.containsIgnoreCase( "nginx" )
    }

    def "Connect to service, port 80, using port forward."()
    {
        given:
        int status
        String body

        when:
        try( KubeConnection connection = instance.getPortForwardConnection( serviceName, 80 ) )
        {
            HttpResponse<String> response = httpGet( connection )
            status = response.statusCode(  )
            body = response.body()
        }

        then:
        status == 200
        body != ""
        body.containsIgnoreCase( "nginx" )
    }

    def "Connect to service, port 80."()
    {
        given:
        int status
        String body

        when:
        try( KubeConnection connection = instance.getConnection( serviceName, 80 ) )
        {
            HttpResponse<String> response = httpGet( connection )
            status = response.statusCode(  )
            body = response.body()
        }

        then:
        status == 200
        body != ""
        body.containsIgnoreCase( "nginx" )
    }

    def "Connect to external service, with internal port 80."()
    {
        given:
        int status
        String body

        when:
        try( KubeConnection connection = instance.getConnection( NginxResources.EXTERNAL_NGINX_SERVICE_NAME, 80 ) )
        {
            HttpResponse<String> response = httpGet( connection )
            status = response.statusCode(  )
            body = response.body()
        }

        then:
        status == 200
        body != ""
        body.containsIgnoreCase( "nginx" )
    }

    HttpResponse<String> httpGet( KubeConnection connection )
    {
        String host = connection.getHostName()
        int port = connection.getPort()

        HttpClient httpClient = HttpClient.newBuilder(  ).build(  )
        HttpRequest request = HttpRequest.newBuilder( )
                .uri( URI.create( "http://" + host + ":" + port +"/" ) )
                .GET( )
                .build(  )

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
