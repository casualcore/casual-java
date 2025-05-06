/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.integration

import se.laz.casual.test.tdk8s.TestKube
import se.laz.casual.test.tdk8s.connection.KubeConnection
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class CasualTest extends Specification
{
    @Shared
    TestKube testKube

    def setupSpec()
    {
        testKube = TestKube.newBuilder(  )
                .addPod( Casual.SIMPLE_CASUAL_POD_NAME, Casual.SIMPLE_CASUAL_POD )
                .addService( Casual.SIMPLE_CASUAL_SERVICE_NAME, Casual.SIMPLE_CASUAL_SERVICE )
                .addPod( CasualJava.SIMPLE_CASUAL_JAVA_POD_NAME, CasualJava.SIMPLE_CASUAL_JAVA_POD )
                .addService( CasualJava.SIMPLE_CASUAL_JAVA_SERVICE_NAME, CasualJava.SIMPLE_CASUAL_JAVA_SERVICE )
                .build(  )
        testKube.init(  )
    }

    def cleanupSpec()
    {
        testKube.destroy( )
    }

    def "Check casual log remotely."()
    {
        when:
        String log = testKube.getController(  ).getLog( Casual.SIMPLE_CASUAL_POD_NAME )

        then:
        log == ""
    }

    def "Call casual via casual-java echo service"()
    {
        //curl -iv -XPOST http://localhost:8080/casual/casual%2Fexample%2Fecho -H 'Content-Type: application/casual-x-octet' -d '{"hi":"there"}'
        given:
        int status
        String body
        String input = "{\"hi\":\"there\"}"

        when:
        try( KubeConnection con = testKube.getConnection( CasualJava.SIMPLE_CASUAL_JAVA_SERVICE_NAME, 8080 ) )
        {
            String host = con.getHostName(  )
            int port = con.getPort(  )

            HttpClient httpClient = HttpClient.newBuilder(  ).build(  )
            HttpRequest request = HttpRequest.newBuilder( )
                    .uri( URI.create( "http://" + host + ":" + port +"/" ).resolve( "casual/casual%2Fexample%2Fecho" ) )
                    .header( "Content-Type", "application/casual-x-octet" )
                    .POST( HttpRequest.BodyPublishers.ofString( input ) )
                    .build(  )

            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            status = response.statusCode(  )
            body = response.body(  )
        }

        then:
        status == 200
        body == input

    }

    def "Call test simple echo service."()
    {
        //curl -iv -XPOST http://localhost:8080/simple/echoFielded -H 'Content-Type: application/json' -d '{"id": 1, "name":"myname"}'
        given:
        int status
        String body
        String input = "{\"id\":1,\"name\":\"myname\"}"

        when:
        try( KubeConnection con = testKube.getConnection( CasualJava.SIMPLE_CASUAL_JAVA_SERVICE_NAME, 8080 ) )
        {
            String host = con.getHostName(  )
            int port = con.getPort(  )

            HttpClient httpClient = HttpClient.newBuilder(  ).build(  )
            HttpRequest request = HttpRequest.newBuilder( )
                    .uri( URI.create( "http://" + host + ":" + port +"/" ).resolve( "simple/echoFielded" ) )
                    .header( "Content-Type", "application/json" )
                    .POST( HttpRequest.BodyPublishers.ofString( input ) )
                    .build(  )

            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            status = response.statusCode(  )
            body = response.body(  )
        }

        then:
        status == 200
        body == input

    }

}
