/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import se.laz.casual.config.json.Domain
import spock.lang.Specification

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class DomainTest extends Specification
{
    Domain instance

    def setup()
    {
        instance = Domain.getFromEnv()
    }

    def "Name returns correct value with env set."()
    {
        given:
        Domain domain
        String domainName = "casual-java-test"

        when:
        withEnvironmentVariable(Domain.DOMAIN_NAME_ENV, domainName).execute( {
            domain = Domain.getFromEnv()
        } )

        then:
        domain.getName() == domainName
    }

    def "Name returns default value without env set."()
    {
        expect:
        instance.getName() == Domain.DOMAIN_NAME_DEFAULT
    }

    def "Id is set."()
    {
        expect:
        instance.getId() != null
    }

}
