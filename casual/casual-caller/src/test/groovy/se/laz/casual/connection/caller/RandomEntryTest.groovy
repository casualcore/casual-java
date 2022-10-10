package se.laz.casual.connection.caller

import se.laz.casual.api.queue.QueueInfo
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

    def 'get queue with 0 entries, should give TPENOENT'()
    {
        given:
        def queueInfo = QueueInfo.of('Battlestar.Galactica')
        def entry = Optional.empty()
        def lookup = Mock(ConnectionFactoryLookup)
        lookup.get(queueInfo) >> {
            entry
        }
        when:
        def actual = lookup.get(queueInfo)
        then:
        !actual.isPresent()
    }

    def 'get service with 1 entry, should return that entry'()
    {
        given:
        def serviceName = 'echo'
        def entries = [ConnectionFactoryEntry.of(Mock(ConnectionFactoryProducer))]
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
        def queueInfo = QueueInfo.of('Battlestar.Galactica')
        def entry = Optional.of(ConnectionFactoryEntry.of(Mock(ConnectionFactoryProducer)))
        def lookup = Mock(ConnectionFactoryLookup)
        lookup.get(queueInfo) >> {
            entry
        }
        when:
        def actual = lookup.get(queueInfo)
        then:
        actual.get() == entry.get()
    }

    def 'getEntry with more than 1 entry should get all entries eventually'()
    {
        given:
        def entryOne = ConnectionFactoryEntry.of(Mock(ConnectionFactoryProducer))
        def entryTwo = ConnectionFactoryEntry.of(Mock(ConnectionFactoryProducer))
        def entryThree = ConnectionFactoryEntry.of(Mock(ConnectionFactoryProducer))
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
