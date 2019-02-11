/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain

import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class CasualDomainConnectRequestMessageTest extends Specification
{
    @Shared
    def syncSink

    def setup()
    {
        syncSink = new LocalByteChannel()
    }

    def "fail - no protocols"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def protocols = []
        when:
        def requestMessage = CasualDomainConnectRequestMessage.createBuilder()
                                                              .withExecution(execution)
                                                              .withDomainId(domainId)
                                                              .withDomainName(domainName)
                                                              .withProtocols(protocols)
                                                              .build()
        then:
        requestMessage == null
        def e = thrown (CasualProtocolException)
        e.message == "zero size protocol list, this makes no sense!"
    }

    def "roundtrip with protocols"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def protocols = [1l,2l,3l]
        def requestMessage = CasualDomainConnectRequestMessage.createBuilder()
                                                              .withExecution(execution)
                                                              .withDomainId(domainId)
                                                              .withDomainName(domainName)
                                                              .withProtocols(protocols)
                                                              .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessageImpl<CasualDomainConnectRequestMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)

        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg == syncResurrectedMsg
        domainName == syncResurrectedMsg.message.domainName
        protocols == syncResurrectedMsg.message.protocols
    }

}
