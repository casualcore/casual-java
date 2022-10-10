package se.laz.casual.connection.caller

import se.laz.casual.connection.caller.util.ConnectionFactoryFinder
import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Specification

import javax.naming.InitialContext
import javax.naming.NameClassPair
import javax.naming.NamingEnumeration

class ConnectionFactoryFinderTest extends Specification
{
    def 'no entries found'()
    {
        given:
        def root = 'asdf'
        def entries = Mock(NamingEnumeration)
        entries.hasMoreElements() >> false
        def context = Mock(InitialContext)
        context.list(root) >> {
            entries
        }
        when:
        def result = ConnectionFactoryFinder.of().findConnectionFactory(root, context)
        then:
        result.isEmpty()
    }

    def 'entry found but is not instance of CasualConnectionFactory'()
    {
        given:
        def root = 'asdf'
        def partJndiName = 'joesConnectionFactory'
        def entries = Mock(NamingEnumeration)
        entries.hasMoreElements() >>> [true,false]
        entries.nextElement() >> {
            def element = Mock(NameClassPair)
            element.isRelative() >> true
            element.getName() >> partJndiName
            return element
        }
        def context = Mock(InitialContext)
        context.list(root) >> {
            entries
        }
        context.lookup(root + "/" + partJndiName) >> {
            'A string and not an instance of CasualConnectionFactory'
        }
        when:
        def result = ConnectionFactoryFinder.of().findConnectionFactory(root, context)
        then:
        result.isEmpty()
    }

    def 'entry found and is instance of CasualConnectionFactory'()
    {
        given:
        def root = 'eis'
        def partJndiName = 'casualConnectionFactory'
        def completeJndiName = root + "/" + partJndiName
        def entries = Mock(NamingEnumeration)
        entries.hasMoreElements() >>> [true,false]
        entries.nextElement() >> {
            def element = Mock(NameClassPair)
            element.isRelative() >> true
            element.getName() >> partJndiName
            return element
        }
        def context = Mock(InitialContext)
        context.list(root) >> {
            entries
        }
        context.lookup(completeJndiName) >> {
            Mock(CasualConnectionFactory)
        }
        when:
        def result = ConnectionFactoryFinder.of().findConnectionFactory(root, context)
        then:
        !result.isEmpty()
        result[0].getJndiName() == completeJndiName
    }


}
