/*
 * Copyright (c) 2024 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test

import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Testcontainers
class WildflyTestContainersTest extends Specification
{
    @Shared
    DockerImageName imageName = DockerImageName.parse("quay.io/wildfly/wildfly:32.0.1.Final-jdk21"  )

    GenericContainer wildfly = new GenericContainer( imageName )
        .withExposedPorts( 8080 )

    def setupSpec()
    {
        System.out.println( imageName.toString(  ) )
        System.out.println( imageName.getRegistry(  ) )
    }

    def "Connect to casual and call echo."()
    {
        given:
        String host = wildfly.getHost(  )
        String port = wildfly.getMappedPort( 8080 )
        String echoPayload = "{ \"hi\": \"there\"}"

        when:
        HttpClient client = HttpClient.newBuilder(  ).build(  )
        HttpRequest request = HttpRequest.newBuilder( )
            .uri( URI.create( host + ":" + port + "/" ) )
            .GET( )
            .build(  )

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        then:
        response.statusCode() == 200
        response.body() == echoPayload
    }
}
