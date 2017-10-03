package se.kodarkatten.casual.jca

import se.kodarkatten.casual.jca.CasualResourceAdapter
import spock.lang.Shared
import spock.lang.Specification

class CasualResourceAdapterTest extends Specification
{
    @Shared CasualResourceAdapter instance

    def setup()
    {
        instance = new CasualResourceAdapter()
    }

    def "GetXAResources"()
    {
        expect:
        instance.getXAResources() == null
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualResourceAdapter" )
    }
}
