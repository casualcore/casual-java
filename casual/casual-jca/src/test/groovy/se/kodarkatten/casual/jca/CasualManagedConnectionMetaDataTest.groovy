package se.kodarkatten.casual.jca

import spock.lang.Shared
import spock.lang.Specification

class CasualManagedConnectionMetaDataTest extends Specification
{
    @Shared
    CasualManagedConnectionMetaData instance

    def setup()
    {
        instance = new CasualManagedConnectionMetaData()
    }

    def "GetEISProductName"()
    {
        expect:
        instance.getEISProductName() != null
    }

    def "GetEISProductVersion"()
    {
        expect:
        instance.getEISProductVersion() != null
    }

    def "GetMaxConnections"()
    {
        expect:
        instance.getMaxConnections() == 0
    }

    def "GetUserName"()
    {
        expect:
        instance.getUserName() != null
    }
}
