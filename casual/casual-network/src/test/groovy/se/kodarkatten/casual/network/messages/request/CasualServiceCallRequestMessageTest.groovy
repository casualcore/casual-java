package se.kodarkatten.casual.network.messages.request

import se.kodarkatten.casual.api.flags.AtmiFlags
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.common.ServiceBuffer
import se.kodarkatten.casual.network.messages.request.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.utils.ByteSink
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by aleph on 2017-03-16.
 */
class CasualServiceCallRequestMessageTest extends Specification
{
    @Shared
    def execution = UUID.randomUUID()
    @Shared
    def callDescriptor = 42
    @Shared
    def serviceName = 'A very fine service'
    @Shared
    def timeout = 90 * 1000
    @Shared
    def parentName = 'Jane Doe'
    @Shared
    def nullXID = XID.of()
    @Shared
    def xatmiFlags = Flag.of(AtmiFlags.TPNOBLOCK)
    @Shared
    def serviceData
    @Shared
    def serviceType = 'application/json'
    @Shared
    def serviceBuffer

    def setupSpec()
    {
        List<byte[]> l = new ArrayList<>()
        l.add([2,3,4] as byte[])
        serviceData = l
        serviceBuffer = ServiceBuffer.of(serviceType, serviceData)
    }

    def "Message creation"()
    {
        setup:
        when:
        def msg = CasualServiceCallRequestMessage.createBuilder()
                                          .setExecution(execution)
                                          .setCallDescriptor(callDescriptor)
                                          .setServiceName(serviceName)
                                          .setTimeout(timeout)
                                          .setParentName(parentName)
                                          .setXid(nullXID)
                                          .setXatmiFlags(xatmiFlags)
                                          .setServiceBuffer(serviceBuffer)
                                          .build()
        then:
        msg.execution == execution
        msg.callDescriptor == callDescriptor
        msg.serviceName == serviceName
        msg.timeout == timeout
        msg.parentName == parentName
        msg.xid == nullXID
        msg.serviceBuffer == serviceBuffer
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE"()
    {
        setup:
        def requestMsg = CasualServiceCallRequestMessage.createBuilder()
                                                        .setExecution(execution)
                                                        .setCallDescriptor(callDescriptor)
                                                        .setServiceName(serviceName)
                                                        .setTimeout(timeout)
                                                        .setParentName(parentName)
                                                        .setXid(nullXID)
                                                        .setXatmiFlags(xatmiFlags)
                                                        .setServiceBuffer(serviceBuffer)
                                                        .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new ByteSink()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg

    }

}
