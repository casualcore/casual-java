/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import spock.lang.Specification

/**
 * Created by aleph on 2017-03-16.
 */
class CasualNWMessageTypeTest extends Specification
{
    def "Test marshalling/unmarshalling"()
    {
        setup:
        def typeValue = CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST.messageId
        when:
        CasualNWMessageType type = CasualNWMessageType.unmarshal(typeValue)
        then:
        type == CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST
    }

    def "Unknown id"()
    {
        setup:
        def typeValue = Integer.MAX_VALUE
        when:
        CasualNWMessageType type = CasualNWMessageType.unmarshal(typeValue)
        then:
        type == null
        IllegalArgumentException e = thrown()
        e.message == "Unknown message type:${typeValue}"
    }
}
