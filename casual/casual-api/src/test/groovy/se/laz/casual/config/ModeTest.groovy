/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import spock.lang.Specification

class ModeTest extends Specification
{
    def "get all modes"()
    {
        expect:
        for( Mode m: Mode.values(  ) )
        {
            Mode m2 = Mode.fromName( m.getName(  ) )
            assert m == m2
            assert m.getName(  ) == m2.getName(  )
        }
    }

    def "Get mode invalid throws IllegalArgumentException"()
    {
        when:
        Mode.fromName( "invalid" )

        then:
        thrown IllegalArgumentException
    }


}
