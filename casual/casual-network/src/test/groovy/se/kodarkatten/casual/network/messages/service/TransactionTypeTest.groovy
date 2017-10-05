package se.kodarkatten.casual.network.messages.service

import se.kodarkatten.casual.network.messages.domain.TransactionType
import spock.lang.Specification

/**
 * Created by aleph on 2017-03-07.
 */
class TransactionTypeTest extends Specification
{
    def "unmarshalling with invalid id"()
    {
        setup:
        short id = TransactionType.NONE.id + 1
        when:
        def t = TransactionType.unmarshal(id)
        then:
        t == null
        final IllegalArgumentException e = thrown ()
        e.message == "TransactionType:${id}"
    }

    def "roundtripping with a valid id"()
    {
        setup:
        def t = TransactionType.JOIN
        when:
        def unmarshalled = TransactionType.unmarshal(TransactionType.marshal(t))
        then:
        t == unmarshalled
    }

}
