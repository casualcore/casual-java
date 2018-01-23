package se.kodarkatten.casual.network.messages.transaction

import se.kodarkatten.casual.api.xa.XAReturnCode
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by aleph on 2017-04-03.
 */
class CasualTransactionResourcePrepareReplyMessageTest extends Specification
{
    @Shared
    def execution = UUID.randomUUID()

    @Shared
    def xid = XID.NULL_XID

    @Shared
    def resourceId = 12345

    @Shared
    def transactionReturnCode = XAReturnCode.XA_OK

    def "Message creation"()
    {
        setup:
        when:
        def msg = CasualTransactionResourcePrepareReplyMessage.of(execution, xid, resourceId, transactionReturnCode)
        then:
        msg.execution == execution
        msg.xid == xid
        msg.resourceId == resourceId
        msg.transactionReturnCode == transactionReturnCode
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE"()
    {
        setup:
        def requestMsg = CasualTransactionResourcePrepareReplyMessage.of(execution, xid, resourceId, transactionReturnCode)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "Roundtrip message - force chunking"()
    {
        setup:
        def requestMsg = CasualTransactionResourcePrepareReplyMessage.of(execution, xid, resourceId, transactionReturnCode)
        requestMsg.setMaxMessageSize(1)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        networkBytes.size() > 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        def requestMsg = CasualTransactionResourcePrepareReplyMessage.of(execution, xid, resourceId, transactionReturnCode)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "Roundtrip message - force chunking, sync"()
    {
        setup:
        def requestMsg = CasualTransactionResourcePrepareReplyMessage.of(execution, xid, resourceId, transactionReturnCode)
        requestMsg.setMaxMessageSize(1)
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        networkBytes.size() > 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

}
