/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.server.messages

import spock.lang.Specification

class ConnectReplyMessageTest extends Specification
{

    def "Equals and hashCode"()
    {
        given:
        ConnectReply reply = ConnectReply.CONNECT_REPLY

        when:
        ConnectReplyMessage instance1 = ConnectReplyMessage.of( reply )
        ConnectReplyMessage instance2 = ConnectReplyMessage.of( reply )

        then:
        instance1 == instance1
        instance2 == instance1
        instance1.hashCode(  ) == instance1.hashCode(  )
        instance1.hashCode() == instance2.hashCode(  )

        !instance1.equals( "String" )
    }
}
