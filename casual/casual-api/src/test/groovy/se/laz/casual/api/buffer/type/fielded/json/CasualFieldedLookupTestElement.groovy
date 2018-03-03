/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.json

import spock.lang.Specification

class CasualFieldedLookupTestElement extends Specification
{
    def 'get URL'()
    {
        when:
        def url = CasualFieldedLookup.getURL();
        then:
        noExceptionThrown()
        null != url
    }

    def 'get names'()
    {
        when:
        def l = CasualFieldedLookup.getNames()
        then:
        noExceptionThrown()
        !l.isEmpty()
    }

    def "lookup realId"()
    {
        setup:
        def name = 'FLD_LONG3'
        when:
        def r = CasualFieldedLookup.forName(name).get().realId
        then:
        r == 67109867
    }

    def "lookup name"()
    {
        setup:
        def realId = 67109867
        when:
        def r = CasualFieldedLookup.forRealId(realId).get().name
        then:
        r == 'FLD_LONG3'
    }

    def "failed lookup"()
    {
        setup:
        def name = 'NOP'
        when:
        def r = CasualFieldedLookup.forName(name)
        then:
        r == Optional.empty()
    }

    def "failed lookup, throw"()
    {
        setup:
        def name = 'NOP'
        when:
        def r = CasualFieldedLookup.forName(name).orElseThrow({throw new RuntimeException(name)})
        then:
        r == null
        def error = thrown(RuntimeException)
        error.message == name
    }

}
