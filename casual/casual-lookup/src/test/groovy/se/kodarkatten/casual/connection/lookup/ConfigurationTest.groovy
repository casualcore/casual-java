package se.kodarkatten.casual.connection.lookup

import spock.lang.Shared
import spock.lang.Specification

class ConfigurationTest extends Specification
{
    @Shared
    def jndiNames = ['one', 'two']

    def 'null list'()
    {
        when:
        new Configuration(null)
        then:
        def e = thrown(NullPointerException)
        e.message == 'jndinames can not be null'
    }

    def 'empty list'()
    {
        when:
        new Configuration([])
        then:
        def e = thrown(CasualLookupException)
        e.message == 'no jndi names for casual instances'
    }

    def 'ok config'()
    {
        when:
        def c = new Configuration(jndiNames)
        then:
        noExceptionThrown()
        c != null
        c.getJNDINames() == jndiNames
    }

}
