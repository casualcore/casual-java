/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.integration

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.Service
import spock.lang.Specification

class Fabric8EqualityChecks extends Specification
{
    def setup()
    {

    }

    def "Pod objects equal."()
    {
        expect:
        WildflyResources.SIMPLE_WILDFLY_POD == WildflyResources.SIMPLE_WILDFLY_POD
    }

    def "Pod objects edited, create new, original not effected."()
    {
        given:
        String originalName = WildflyResources.SIMPLE_WILDFLY_POD.getMetadata(  ).getName(  )

        when:
        Pod updated = WildflyResources.SIMPLE_WILDFLY_POD.edit(  )
                .editOrNewMetadata(  )
                .withName( "updated" )
                .endMetadata(  )
                .build(  )

        then:
        updated.getMetadata(  ).getName(  ) != originalName
        WildflyResources.SIMPLE_WILDFLY_POD.getMetadata(  ).getName(  ) == originalName
        WildflyResources.SIMPLE_WILDFLY_POD != updated
    }

    def "Pod object editied without changes still equal."()
    {
        when:
        Pod copy = WildflyResources.SIMPLE_WILDFLY_POD.edit(  ).build(  )

        then:
        copy == WildflyResources.SIMPLE_WILDFLY_POD
    }

    def "Service objects equal"()
    {
        expect:
        WildflyResources.SIMPLE_WILDFLY_SERVICE == WildflyResources.SIMPLE_WILDFLY_SERVICE
        WildflyResources.EXTERNAL_WILDFLY_SERVICE == WildflyResources.EXTERNAL_WILDFLY_SERVICE
    }

    def "Service edited, creates new, original not effected."()
    {
        given:
        String originalName = WildflyResources.EXTERNAL_WILDFLY_SERVICE.getMetadata(  ).getName(  )

        when:
        Service updated = WildflyResources.EXTERNAL_WILDFLY_SERVICE.edit(  )
                .editOrNewMetadata(  )
                .withName( "updated" )
                .endMetadata(  )
                .build(  )

        then:
        updated.getMetadata(  ).getName(  ) != originalName
        WildflyResources.EXTERNAL_WILDFLY_SERVICE.getMetadata(  ).getName(  ) == originalName
        updated != WildflyResources.EXTERNAL_WILDFLY_SERVICE
    }

    def "Service object edited without changes still equal."()
    {
        when:
        Service copy = WildflyResources.EXTERNAL_WILDFLY_SERVICE.edit(  ).build(  )

        then:
        copy == WildflyResources.EXTERNAL_WILDFLY_SERVICE
    }

}
