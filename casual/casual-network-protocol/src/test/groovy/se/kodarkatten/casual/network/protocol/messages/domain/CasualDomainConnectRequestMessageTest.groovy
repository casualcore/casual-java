package se.kodarkatten.casual.network.protocol.messages.domain

import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import se.kodarkatten.casual.network.protocol.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class CasualDomainConnectRequestMessageTest extends Specification
{
    @Shared
    def asyncSink
    @Shared
    def syncSink

    def setup()
    {
        asyncSink = new LocalAsyncByteChannel()
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
        CasualNWMessageImpl<CasualDomainConnectRequestMessage> asyncResurrectedMsg = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessageImpl<CasualDomainConnectRequestMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)

        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
        domainName == asyncResurrectedMsg.message.domainName
        protocols == asyncResurrectedMsg.message.protocols
    }

}
