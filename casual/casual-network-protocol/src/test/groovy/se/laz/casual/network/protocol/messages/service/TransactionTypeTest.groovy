/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service

import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Specification

/**
 * Created by aleph on 2017-03-07.
 */
class TransactionTypeTest extends Specification
{
    def "unmarshalling with invalid id"()
    {
        setup:
        short id = TransactionType.BRANCH.id + 1
        when:
        def t = TransactionType.unmarshal(id)
        then:
        t == null
        final IllegalArgumentException e = thrown ()
        e.message == "TransactionType:${id}"
    }

    def "roundtripping with a valid id"()
    {
        expect:
        def unmarshalled = TransactionType.unmarshal(TransactionType.marshal(transactionType))
        transactionType == unmarshalled
        where:
        transactionType           | _
        TransactionType.AUTOMATIC | _
        TransactionType.JOIN      | _
        TransactionType.ATOMIC    | _
        TransactionType.NONE      | _
        TransactionType.BRANCH    | _
    }

}
