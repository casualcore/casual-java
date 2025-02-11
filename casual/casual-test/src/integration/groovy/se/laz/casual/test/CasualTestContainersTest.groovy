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
class CasualTestContainersTest extends Specification
{
    @Shared
    DockerImageName imageName = DockerImageName.parse("192.168.68.130:5000/casual:0.0.1-SNAPSHOT"  )

    GenericContainer casual = new GenericContainer( imageName )
        .withExposedPorts( 7771 )
        .withEnv( ["CASUAL_LOG_PATH":"logs" ] )

    GenericContainer casual_jca = new GenericContainer( "192.168.68.130:5000/casual-java:3.2.50-SNAPSHOT" )
        .withExposedPorts( 8080 )
        .withEnv( [
                "CASUAL_HOST": casual.getHost(),
                "CASUAL_PORT": "7771",
                "CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT": "java:/eis",
                "CASUAL_FIELD_TABLE": "/opt/jboss/wildfly/casual/configs/casual-fields.json"
        ] )

//    - name: CASUAL_HOST
//        value: casual-test
//    - name: CASUAL_PORT
//        value: "7771"
//    #        - name: CASUAL_INBOUND_STARTUP_MODE
//    #          value: "trigger"
//    #- name: CASUAL_CONFIG_FILE
//    #  value: /opt/jboss/wildfly/casual/configs/casual-config-inbound-initial-delay.json
//    - name: CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL
//        value: "TRACE"
//    - name: CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT
//        value: "java:/eis"
//    - name: CASUAL_FIELD_TABLE
//        value: "/opt/jboss/wildfly/casual/configs/casual-fields.json"

    def setupSpec()
    {
        System.out.println( imageName.toString(  ) )
        System.out.println( imageName.getRegistry(  ) )
        assert imageName.getRegistry(  ) == "192.168.68.130:5000"
        assert imageName.getRepository(  ) == "casual"
        assert imageName.getVersionPart(  ) == "0.0.1-SNAPSHOT"
    }

    def "Connect to casual and call echo."()
    {
        given:
        String host = casual_jca.getHost(  )
        String echoPayload = "{ \"hi\": \"there\"}"

        when:
        HttpClient client = HttpClient.newBuilder(  )
        HttpRequest request = HttpRequest.newBuilder( )
            .uri( URI.create( host + "/casual/casual%2Fexample%2Fecho" ) )
            .header("Content-Type", "application/casual-x-octet")
            .POST( HttpRequest.BodyPublishers.ofString( echoPayload  ) )

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        then:
        response.statusCode() == 200
        response.body() == echoPayload
    }
}
