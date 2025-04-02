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

class NginxResources
{

    public static final Map<String, String> SELECTOR = Maps.of( "app", "nginx" )

    public static final String SIMPLE_NGINX_POD_NAME = "nginx-test"

    public static final Pod SIMPLE_NGINX_POD = new PodBuilder()
            .withNewMetadata()
            .withName( SIMPLE_NGINX_POD_NAME )
            .addToLabels( SELECTOR )
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName( "nginx" )
            .withImage( "nginx:1.27.4" )
            .addNewPort().withContainerPort( 80 ).endPort()
            .withNewReadinessProbe()
            .withNewTcpSocket()
            .withNewPort()
            .withValue( 80 )
            .endPort()
            .endTcpSocket()
            .endReadinessProbe()
            .endContainer()
            .endSpec()
            .build()

    public static final String SIMPLE_NGINX_SERVICE_NAME = "nginx-service"

    public static final Service SIMPLE_NGINX_SERVICE = new ServiceBuilder()
            .withNewMetadata()
                .withName( SIMPLE_NGINX_SERVICE_NAME )
            .endMetadata()
            .withNewSpec()
                .addToSelector( SELECTOR )
                .addNewPort().withName( "http" ).withPort( 80 ).endPort()
            .endSpec()
            .build()

    public static final String EXTERNAL_NGINX_SERVICE_NAME = "nginx-external"

    public static final Service EXTERNAL_NGINX_SERVICE = new ServiceBuilder()
            .withNewMetadata()
            .withName( EXTERNAL_NGINX_SERVICE_NAME )
            .endMetadata()
            .withNewSpec()
            .addToSelector( SELECTOR )
            .addNewPort()
                .withName( "http" )
                .withPort( 61819 ) //hope this is free!!
                .withTargetPort( new IntOrString( 80 ) )
            .endPort()
            .withType("LoadBalancer" )
            .endSpec()
            .build()

}
