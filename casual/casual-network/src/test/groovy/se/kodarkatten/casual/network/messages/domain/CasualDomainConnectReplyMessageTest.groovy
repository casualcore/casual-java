package se.kodarkatten.casual.network.messages.domain

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
import se.kodarkatten.casual.network.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

class CasualDomainConnectReplyMessageTest extends Specification
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
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMessage)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessage<CasualDomainConnectReplyMessage> asyncResurrectedMsg = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessage<CasualDomainConnectReplyMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        networkBytes.size() == 2 // header + msg
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }



}
