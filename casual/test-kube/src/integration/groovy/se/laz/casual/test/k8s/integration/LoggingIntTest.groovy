/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.integration

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import se.laz.casual.test.k8s.TestKube
import spock.lang.Shared
import spock.lang.Specification

class LoggingIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = LoggingIntTest.class.getSimpleName(  )
    @Shared
    TestKube instance

    def setupSpec()
    {

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0

        instance = TestKube.newBuilder()
                .label( id )
                .addPod( NginxResources.SIMPLE_NGINX_POD_NAME, NginxResources.SIMPLE_NGINX_POD )
                .build()

        instance.init(  )

    }

    def cleanupSpec()
    {
        instance.destroy(  )

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0
    }

    def "Retrieve log from a pod."()
    {
        when:
        String log = client.pods().withName( NginxResources.SIMPLE_NGINX_POD_NAME )
                .getLog()

        then:
        log != ""
        log.containsIgnoreCase( "nginx" )
    }

    def "Retrieve log from a pod, tailing."()
    {
        when:
        String log = client.pods().withName( NginxResources.SIMPLE_NGINX_POD_NAME )
                .tailingLines( 10 )
                .getLog()

        then:
        log != ""
        log.containsIgnoreCase( "start worker process" )
    }
}
