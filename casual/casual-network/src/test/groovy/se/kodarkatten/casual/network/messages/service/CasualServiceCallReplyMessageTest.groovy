package se.kodarkatten.casual.network.messages.service

import se.kodarkatten.casual.api.flags.ErrorState
import se.kodarkatten.casual.api.flags.TransactionState
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.utils.ByteUtils
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

/**
 * Created by aleph on 2017-03-28.
 */
class CasualServiceCallReplyMessageTest extends Specification
{
    @Shared
    def execution = UUID.randomUUID()
    @Shared
    def callError = ErrorState.TPENOENT
    @Shared
    def userError = 45656l
    @Shared
    def nullXID = XID.NULL_XID
    @Shared
    def transactionState = TransactionState.ROLLBACK_ONLY
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
        def msg = CasualServiceCallReplyMessage.createBuilder()
                                               .setExecution(execution)
                                               .setError(callError)
                                               .setUserSuppliedError(userError)
                                               .setXid(nullXID)
                                               .setTransactionState(transactionState)
                                               .setServiceBuffer(serviceBuffer)
                                               .build()
        then:
        msg.getExecution() == execution
        msg.getError() == callError
        msg.getUserDefinedCode() == userError
        msg.getXid() == nullXID
        msg.getTransactionState() == transactionState
        msg.getServiceBuffer() == serviceBuffer
        msg.getServiceBuffer().payload == serviceBuffer.payload
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE"()
    {
        setup:
        def requestMsg = CasualServiceCallReplyMessage.createBuilder()
                .setExecution(execution)
                .setError(callError)
                .setUserSuppliedError(userError)
                .setXid(nullXID)
                .setTransactionState(transactionState)
                .setServiceBuffer(serviceBuffer)
                .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == 1
        Arrays.deepEquals( requestMsg.getServiceBuffer().getPayload().toArray(), resurrectedMsg.getMessage().getServiceBuffer().getPayload().toArray() )
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - forcing chunking"()
    {
        setup:
        def requestMsg = CasualServiceCallReplyMessage.createBuilder()
                .setExecution(execution)
                .setError(callError)
                .setUserSuppliedError(userError)
                .setXid(nullXID)
                .setTransactionState(transactionState)
                .setServiceBuffer(serviceBuffer)
                .build()
        // force write chunking
        requestMsg.setMaxMessageSize(1)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        def collectedServicePayload = collectServicePayload(resurrectedMsg.getMessage().getServiceBuffer().getPayload())

        then:
        networkBytes != null
        networkBytes.size() > 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == serviceBuffer.payload.get(0).length
        Arrays.deepEquals( requestMsg.getServiceBuffer().getPayload().toArray(), collectedServicePayload.toArray( ) )
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        def requestMsg = CasualServiceCallReplyMessage.createBuilder()
                .setExecution(execution)
                .setError(callError)
                .setUserSuppliedError(userError)
                .setXid(nullXID)
                .setTransactionState(transactionState)
                .setServiceBuffer(serviceBuffer)
                .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == 1
        requestMsg.serviceBuffer.payload == resurrectedMsg.getMessage().getServiceBuffer().payload
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - forcing chunking, sync"()
    {
        setup:
        def requestMsg = CasualServiceCallReplyMessage.createBuilder()
                .setExecution(execution)
                .setError(callError)
                .setUserSuppliedError(userError)
                .setXid(nullXID)
                .setTransactionState(transactionState)
                .setServiceBuffer(serviceBuffer)
                .build()
        // force write chunking
        requestMsg.setMaxMessageSize(1)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        def collectedServicePayload = collectServicePayload(resurrectedMsg.getMessage().getServiceBuffer().getPayload())

        then:
        networkBytes != null
        networkBytes.size() > 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == serviceBuffer.payload.get(0).length
        requestMsg.getServiceBuffer().getPayload() == collectedServicePayload
    }

    def collectServicePayload(List<byte[]> bytes)
    {
        ByteBuffer b = ByteBuffer.allocate((int)ByteUtils.sumNumberOfBytes(bytes))
        bytes.stream()
                .forEach({d -> b.put(d)})
        List<byte[]> l = new ArrayList<>()
        l.add(b.array())
        return l
    }

}
