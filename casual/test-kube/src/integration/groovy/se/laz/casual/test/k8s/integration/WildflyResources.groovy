/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.integration

import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.PodBuilder
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceBuilder
import org.apache.groovy.util.Maps

class WildflyResources
{

    public static final Map<String, String> SELECTOR = Maps.of( "app", "wildfly" )

    public static final String SIMPLE_WILDFLY_POD_NAME = "wildfly-test"

    public static final Pod SIMPLE_WILDFLY_POD = new PodBuilder()
            .withNewMetadata()
            .withName( SIMPLE_WILDFLY_POD_NAME )
            .addToLabels( SELECTOR )
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName( "wildfly" )
            .withImage( "quay.io/wildfly/wildfly:32.0.1.Final-jdk21" )
            .addNewPort().withContainerPort( 8080 ).endPort()
            .addNewPort().withContainerPort( 9990 ).endPort()
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

    public static final String SIMPLE_WILDFLY_SERVICE_NAME = "wildfly-service"

    public static final Service SIMPLE_WILDFLY_SERVICE = new ServiceBuilder()
            .withNewMetadata()
                .withName( SIMPLE_WILDFLY_SERVICE_NAME )
            .endMetadata()
            .withNewSpec()
                .addToSelector( SELECTOR )
                .addNewPort().withName( "admin" ).withPort( 9990 ).endPort()
                .addNewPort().withName( "http" ).withPort( 8080 ).endPort()
            .endSpec()
            .build()

    public static final String EXTERNAL_WILDFLY_SERVICE_NAME = "wildfly-external"

    public static final Service EXTERNAL_WILDFLY_SERVICE = new ServiceBuilder()
            .withNewMetadata()
            .withName( EXTERNAL_WILDFLY_SERVICE_NAME )
            .endMetadata()
            .withNewSpec()
            .addToSelector( SELECTOR )
            .addNewPort()
                .withName( "http" )
                .withPort( 61818 ) //hope this is free!!
                .withTargetPort( new IntOrString( 8080 ) )
            .endPort()
            .withType("LoadBalancer" )
            .endSpec()
            .build()

}
