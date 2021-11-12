/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.conversation

import spock.lang.Specification

class DuplexTest extends Specification
{
    def 'unmarshalling'()
    {
        when:
        def actualDuplex = Duplex.unmarshall(value)
        then:
        actualDuplex == expectedDuplex
        where:
        value | expectedDuplex
        0     | Duplex.SEND
        1     | Duplex.RECEIVE
    }

    def 'out of range'()
    {
        when:
        Duplex.unmarshall(2)
        then:
        thrown(IllegalArgumentException)
    }

}
