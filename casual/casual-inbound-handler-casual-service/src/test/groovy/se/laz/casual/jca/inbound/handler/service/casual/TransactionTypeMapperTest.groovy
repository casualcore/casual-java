/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual

import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Specification
import spock.lang.Unroll

import javax.ejb.TransactionAttributeType

class TransactionTypeMapperTest extends Specification
{

    /**
     * MANDATORY	    Join
     * NEVER			None
     * NOT_SUPPORTED	None
     * REQUIRED		    Auto
     * REQUIRES_NEW	    Atomic
     * SUPPORTS		    Join
     * @return
     */
    @Unroll
    def "Test mappings #attribute to #result"()
    {
        when:
        TransactionType type = TransactionTypeMapper.map( attribute )

        then:
        type == result

        where:
        attribute                               | result
        TransactionAttributeType.MANDATORY      | TransactionType.JOIN
        TransactionAttributeType.NEVER          | TransactionType.NONE
        TransactionAttributeType.NOT_SUPPORTED  | TransactionType.NONE
        TransactionAttributeType.REQUIRED       | TransactionType.AUTOMATIC
        TransactionAttributeType.REQUIRES_NEW   | TransactionType.ATOMIC
        TransactionAttributeType.SUPPORTS       | TransactionType.JOIN
    }

    def "Null attribute type throws NullPointerException."()
    {
        when:
        TransactionTypeMapper.map( null )

        then:
        thrown NullPointerException.class
    }
}
