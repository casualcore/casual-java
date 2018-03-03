/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup

import spock.lang.Shared
import spock.lang.Specification

class ConfigurationProviderTest extends Specification
{
    @Shared
    def provider = new ConfigurationProvider()
    @Shared
    def resource = 'casual-lookup-config.json'

    def setup()
    {
        Properties p = System.getProperties()
        p.setProperty(ConfigurationProvider.SYSTEM_PROPERTY_NAME, getClass().getClassLoader().getResource(resource).toURI().path)
    }

    def 'initialization test'()
    {
        when:
        provider.initialize()
        def l = provider.getCasualJNDINames()
        then:
        noExceptionThrown()
        l.size() == 2
    }
}
