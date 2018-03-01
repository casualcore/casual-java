package se.kodarkatten.casual.network.protocol.messages.domain

import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class CasualDomainConnectReplyMessageTest extends Specification
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
        def domainId = UUID.randomUUID()
        def domainName = 'Casually owned domain'
        def protocol = 1l
        def requestMessage = CasualDomainConnectReplyMessage.createBuilder()
                                                            .withExecution(execution)
                                                            .withDomainId(domainId)
                                                            .withDomainName(domainName)
                                                            .withProtocolVersion(protocol)
                                                            .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMessage)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessageImpl<CasualDomainConnectReplyMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg == syncResurrectedMsg
    }

}
