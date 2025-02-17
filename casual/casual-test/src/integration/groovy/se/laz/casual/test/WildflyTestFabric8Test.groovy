/*
 * Copyright (c) 2024 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import spock.lang.Shared
import spock.lang.Specification

class WildflyTestFabric8Test extends Specification
{

    @Shared
    KubernetesClient client = new KubernetesClientBuilder().build(  )

    def setupSpec()
    {
        System.out.println( client.getKubernetesVersion(  ) )
    }

    def "Retrieve pods"()
    {
        when:
        List<Pod> list = client.pods(  ).list().getItems(  )

        then:
        list.size(  ) == 4
    }

    def "Retrieve deployments"()
    {
        when:
        List<Deployment> list = client.apps(  ).deployments(  ).list().getItems(  )

        then:
        list.size(  ) == 0
    }

    def "Retrieve pvcs"()
    {
        when:
        List<PersistentVolumeClaim> list = client.persistentVolumeClaims(  ).list().getItems(  )

        then:
        list.size() != 0
    }

    def "Retrieve services"()
    {
        when:
        List<Service> list = client.services(  ).list().getItems(  )

        then:
        list.size(  ) != 0
    }
}
