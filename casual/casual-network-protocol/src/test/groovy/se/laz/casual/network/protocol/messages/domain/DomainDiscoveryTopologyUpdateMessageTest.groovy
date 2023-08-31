/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.domain

import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class DomainDiscoveryTopologyUpdateMessageTest extends Specification
{
    @Shared
    def syncSink

    def setup()
    {
        syncSink = new LocalByteChannel()
    }

    def "roundtrip"()
    {
        setup:
        def execution = UUID.randomUUID()
        def domainsSize = 1L
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def requestMessage = DomainDiscoveryTopologyUpdateMessage.createBuilder()
                                                                         .withExecution(execution)
                                                                         .withDomainsSize(domainsSize)
                                                                         .withDomainId(domainId)
                                                                         .withDomainName(domainName)
                                                                         .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessageImpl<DomainDiscoveryTopologyUpdateMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)

        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg == syncResurrectedMsg
        domainName == syncResurrectedMsg.message.domainName
        domainsSize == syncResurrectedMsg.message.domainsSize
    }

}
