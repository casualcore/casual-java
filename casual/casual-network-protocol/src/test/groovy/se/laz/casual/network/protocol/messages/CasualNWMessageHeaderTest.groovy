/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import spock.lang.Specification

/**
 * Created by aleph on 2017-02-24.
 */
class CasualNWMessageHeaderTest extends Specification
{
    def "Message with payload fitting in one message"()
    {
        setup:
        def type = CasualNWMessageType.SERVICE_CALL_REQUEST
        def correlationId = UUID.randomUUID()
        def payloadSize = 1024
        when:
        def header = CasualNWMessageHeader.createBuilder()
                .setType(type)
                .setCorrelationId(correlationId)
                .setPayloadSize(payloadSize)
                .build()
        then:
        null != header
        header.getType() == type
        header.getCorrelationId() == correlationId
        header.getPayloadSize() == payloadSize
    }
}
