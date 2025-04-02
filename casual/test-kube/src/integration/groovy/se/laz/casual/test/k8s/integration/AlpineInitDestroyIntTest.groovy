/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.integration


import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import se.laz.casual.test.k8s.TestKube
import spock.lang.Shared
import spock.lang.Specification

class AlpineInitDestroyIntTest extends Specification
{
    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build()
    @Shared
    String id = AlpineInitDestroyIntTest.class.getSimpleName(  )

    def setupSpec()
    {
        assert client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
        assert client.services(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
    }

    def cleanupSpec()
    {
        assert client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
        assert client.services(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
    }

    def "Create TestKube with a single pod resource."()
    {
        given:
        TestKube instance = TestKube.newBuilder()
                .label( id )
                .addPod( AlpineResources.SIMPLE_ALPINE_POD_NAME, AlpineResources.SIMPLE_ALPINE_POD )
                .build()

        when:
        instance.init()

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 1

        when:
        instance.destroy()

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
    }

    def "Create TestKube with a single pod resource async."()
    {
        given:
        TestKube instance = TestKube.newBuilder()
                .label( id )
                .addPod( AlpineResources.SIMPLE_ALPINE_POD_NAME, AlpineResources.SIMPLE_ALPINE_POD )
                .build()

        when:
        instance.getController().initAsync(  )

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 1

        when:
        instance.getController(  ).waitUntilReady(  )

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 1

        when:
        instance.getController().destroyAsync(  )

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 1

        when:
        instance.getController().waitUntilDestroyed(  )

        then:
        client.pods(  ).withLabel( "TestKube", id ).list().getItems(  ).size(  ) == 0
    }
}
