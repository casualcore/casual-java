package se.kodarkatten.casual.network.messages.domain

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
import se.kodarkatten.casual.network.utils.TestUtils
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
        def e = thrown (CasualTransportException)
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMessage)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessage<CasualDomainConnectRequestMessage> asyncResurrectedMsg = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessage<CasualDomainConnectRequestMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)

        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
        domainName == asyncResurrectedMsg.message.domainName
        protocols == asyncResurrectedMsg.message.protocols
    }

}
