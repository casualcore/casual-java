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

public class Casual
{
    private Casual()
    {

    }
    public static final String SIMPLE_CASUAL_POD_NAME = "casual";

    public static final Map<String, String> SELECTOR = Map.of( "app", "casual-app" );

    public static final String SIMPLE_CASUAL_SERVICE_NAME = "casual-service";

    public static final Pod SIMPLE_CASUAL_POD = new PodBuilder()
            .withNewMetadata()
                .withName( SIMPLE_CASUAL_POD_NAME )
                .addToLabels( SELECTOR )
            .endMetadata()
            .withNewSpec()
                .addNewContainer()
                    .withName( "casual" )
                    .withImage( "192.168.68.130:5000/casual:0.0.1-SNAPSHOT" )
                    .addNewPort().withContainerPort( 7771 ).endPort()
                    .withNewReadinessProbe()
                        .withNewTcpSocket()
                            .withNewPort()
                                .withValue( 7771 )
                            .endPort()
                        .endTcpSocket()
                    .endReadinessProbe()
                    .addNewEnv().withName( "CASUAL_LOG_PATH" ).withValue( "logs" ).endEnv()
                .endContainer()
            .endSpec()
            .build();

    public static final Service SIMPLE_CASUAL_SERVICE = new ServiceBuilder()
            .withNewMetadata()
            .withName( SIMPLE_CASUAL_SERVICE_NAME )
            .endMetadata()
            .withNewSpec()
            .addToSelector( SELECTOR )
            .addNewPort().withName( "outbound" ).withPort( 7771 ).endPort()
            .endSpec()
            .build();
}
