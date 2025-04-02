/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.runtime

import com.github.stefanbirkner.systemlambda.SystemLambda
import spock.lang.Specification

class ContainerAwarenessTest extends Specification
{
    def "Is this running in a container or not."()
    {
        when:
        SystemLambda.withEnvironmentVariable( ContainerAwareness.CONTAINER_ENV, value ).execute {
            ContainerAwareness.init()
        }

        then:
        ContainerAwareness.inContainer() == expected

        where:
        value    || expected
        "oci"    || true
        "docker" || true
        "lXc"    || true
        "blah"   || true
        ""       || false
        null     || false
    }


}
