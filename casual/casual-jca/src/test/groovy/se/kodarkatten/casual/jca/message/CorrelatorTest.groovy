package se.kodarkatten.casual.jca.message

import se.kodarkatten.casual.jca.message.impl.CorrelatorImpl
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class CorrelatorTest extends Specification
{
    @Shared
    def instance = CorrelatorImpl.of()

    def 'initial empty'()
    {
        expect:
        instance.isEmpty()
    }

    def 'with request'()
    {
        when:
        instance.put(UUID.randomUUID(), new CompletableFuture<Object>())
        then:
        !instance.isEmpty()
    }
}
