package se.kodarkatten.casual.jca.inflow

import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.ResourceAdapter

class CasualActivationSpecTest extends Specification
{
    @Shared CasualActivationSpec instance
    @Shared ResourceAdapter adapter

    def setup()
    {
        instance = new CasualActivationSpec()
        adapter = Mock(ResourceAdapter)
    }

    def "GetResourceAdapter is null initially."()
    {
        expect:
        instance.getResourceAdapter() == null
    }

    def "SetResourceAdapter is saved and can be retrieved with GetResourceAdapter"()
    {
        when:
        instance.setResourceAdapter( adapter )

        then:
        instance.getResourceAdapter() == adapter
    }
}
