/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.integration;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;

import java.util.Map;

public class CasualJava
{
    private CasualJava()
    {

    }
    public static final String SIMPLE_CASUAL_JAVA_POD_NAME = "casual-java";

    public static final Map<String, String> SELECTOR = Map.of( "app", "casual-java-app" );

    public static final String SIMPLE_CASUAL_JAVA_SERVICE_NAME = "casual-java-service";

    public static final Pod SIMPLE_CASUAL_JAVA_POD = new PodBuilder()
            .withNewMetadata()
                .withName( SIMPLE_CASUAL_JAVA_POD_NAME )
                .addToLabels( SELECTOR )
            .endMetadata()
            .withNewSpec()
                .addNewContainer()
                    .withName( "casual-java" )
                    .withImage( "192.168.68.130:5000/casual-java:3.2.50-SNAPSHOT" )
                    .addNewPort().withContainerPort( 7771 ).endPort()
                    .addNewPort().withContainerPort( 7772 ).endPort()
                    .addNewPort().withContainerPort( 8080 ).endPort()
                    .addNewPort().withContainerPort( 9990 ).endPort()
                    .withNewReadinessProbe()
                        .withNewTcpSocket()
                            .withNewPort()
                                .withValue( 7772 )
                            .endPort()
                        .endTcpSocket()
                    .endReadinessProbe()
                    .addNewEnv().withName( "CASUAL_HOST" ).withValue( Casual.SIMPLE_CASUAL_SERVICE_NAME ).endEnv()
                    .addNewEnv().withName( "CASUAL_PORT" ).withValue( "7771" ).endEnv()
                    .addNewEnv().withName( "CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT" ).withValue( "java:/eis" ).endEnv()
                    .addNewEnv().withName( "CASUAL_FIELD_TABLE" ).withValue( "/opt/jboss/wildfly/casual/configs/casual-fields.json" ).endEnv()
                    .addNewEnv().withName( "CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL" ).withValue( "TRACE" ).endEnv()
                .endContainer()
            .endSpec()
            .build();

    public static final Service SIMPLE_CASUAL_JAVA_SERVICE = new ServiceBuilder()
            .withNewMetadata()
            .withName( SIMPLE_CASUAL_JAVA_SERVICE_NAME )
            .endMetadata()
            .withNewSpec()
            .addToSelector( SELECTOR )
            .addNewPort().withName( "output" ).withPort( 7771 ).endPort()
            .addNewPort().withName( "inbound" ).withPort( 7772 ).endPort()
            .addNewPort().withName( "http" ).withPort( 8080 ).endPort()
            .addNewPort().withName( "admin" ).withPort( 9990 ).endPort()
            .endSpec()
            .build();
}
