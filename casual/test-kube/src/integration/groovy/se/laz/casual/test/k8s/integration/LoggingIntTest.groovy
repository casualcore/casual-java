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

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LoggingIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = LoggingIntTest.class.getSimpleName(  )
    @Shared
    TestKube instance
    @Shared
    String podName = NginxResources.SIMPLE_NGINX_POD_NAME

    @Shared
    ZonedDateTime now = ZonedDateTime.now()
    @Shared
    ZonedDateTime afterInit

    def setupSpec()
    {

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0

        instance = TestKube.newBuilder()
                .label( id )
                .addPod( podName, NginxResources.SIMPLE_NGINX_POD )
                .build()

        instance.init(  )
        afterInit = ZonedDateTime.now()

    }

    def cleanupSpec()
    {
        instance.destroy(  )

        List<Pod> pods = client.pods(  ).withLabel( "TestKube", id ).list().getItems(  )

        assert pods.size(  ) == 0
    }

    def "Retrieve full log from a pod."()
    {
        when:
        String actual = instance.getController().getLog( podName )

        then:
        actual != ""
        actual.containsIgnoreCase( "nginx" )
    }

    def "Retrieve last 10 lines of a pod."()
    {
        when:
        String fullLog = instance.getController().getLog( podName )
        String actual = instance.getController().getLogTail( podName, 10 )

        then:
        actual != ""
        actual.containsIgnoreCase( "start worker process" )
        actual.split( "\n" ).size(  ) == 10
        fullLog.endsWith( actual )
    }

    def "Retreive logs since date."()
    {
        when:
        String fullLog = instance.getController().getLog( podName )
        String fromNowLog = instance.getController().getLogSince( podName, now.format( DateTimeFormatter.ISO_OFFSET_DATE_TIME ) )
        String afterInitLog = instance.getController().getLogSince( podName, afterInit.format( DateTimeFormatter.ISO_OFFSET_DATE_TIME ))

        then:
        fullLog != ""
        fullLog.containsIgnoreCase( "nginx" )
        fullLog == fromNowLog
        afterInitLog != fromNowLog
    }
}
