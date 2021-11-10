package se.laz.casual.connection.caller

import se.laz.casual.api.queue.QueueInfo
import se.laz.casual.jca.CasualConnectionFactory
import spock.lang.Specification

class RandomEntryTest extends Specification
{
    def 'get service with 0 entries, should throw'()
    {
        given:
        def serviceName = 'echo'
        def entries = []
        def lookup = Mock(ConnectionFactoryLookup)
        lookup.get(serviceName) >> {
            entries
        }
        when:
        def entry = RandomEntry.getEntry(lookup.get(serviceName))
        then:
        !entry.isPresent()
    }

    def 'get queue with 0 entries, should throw'()
    {
        given:
        def queueInfo = QueueInfo.createBuilder().withQueueName('Battlestar.Galactica').build()
        def entries = []
        def lookup = Mock(ConnectionFactoryLookup)
        lookup.get(queueInfo) >> {
            entries
        }
        when:
        def entry = RandomEntry.getEntry(lookup.get(queueInfo))
        then:
        !entry.isPresent()
    }

    def 'get service with 1 entry, should return that entry'()
    {
        given:
        def serviceName = 'echo'
        def entries = [ConnectionFactoryEntry.of('some-jndi', Mock(CasualConnectionFactory))]
        def lookup = Mock(ConnectionFactoryLookup)
        lookup.get(serviceName) >> {
            entries
        }
        when:
        def actual = RandomEntry.getEntry(lookup.get(serviceName))
        then:
        actual.get() == entries[0]
    }

    def 'get queue with 1 entry, should return that entry'()
    {
        given:
        def queueInfo = QueueInfo.createBuilder().withQueueName('Battlestar.Galactica').build()
        def entries = [ConnectionFactoryEntry.of('some-other-jndi', Mock(CasualConnectionFactory))]
        def lookup = Mock(ConnectionFactoryLookup)
        lookup.get(queueInfo) >> {
            entries
        }
        when:
        def actual = RandomEntry.getEntry(lookup.get(queueInfo))
        then:
        actual.get() == entries[0]
    }

    def 'getEntry with more than 1 entry should get all entries eventually'()
    {
        given:
        def entryOne = ConnectionFactoryEntry.of('some-jndi', Mock(CasualConnectionFactory))
        def entryTwo = ConnectionFactoryEntry.of('some-other-jndi', Mock(CasualConnectionFactory))
        def entryThree = ConnectionFactoryEntry.of('some-other-jndi-alas', Mock(CasualConnectionFactory))
        def cachedEntries = [entryOne, entryTwo, entryThree]
        def possibleEntries = [entryOne, entryTwo, entryThree]
        when:
        while(possibleEntries.size() > 0) {
            def entry = RandomEntry.getEntry(cachedEntries)
            entry.ifPresent({theEntry -> possibleEntries.remove(theEntry)})
        }
        then:
        possibleEntries.size() == 0
    }
}
