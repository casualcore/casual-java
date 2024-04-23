/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.client

import spock.lang.Specification

class ConnectionInformationTest extends Specification
{
    def 'host can not be null'()
    {
        when:
        new ConnectionInformation(null, 4242)
        then:
        thrown(NullPointerException)
    }
}
