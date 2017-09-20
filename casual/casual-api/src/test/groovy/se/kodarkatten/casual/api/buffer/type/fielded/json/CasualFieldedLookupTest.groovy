package se.kodarkatten.casual.api.buffer.type.fielded.json

import spock.lang.Specification

class CasualFieldedLookupTest extends Specification
{
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
