package se.kodarkatten.casual.network.protocol.messages.transaction

import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.flags.XAFlags
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.protocol.io.CasualNetworkReader
import se.kodarkatten.casual.network.protocol.io.CasualNetworkWriter
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl
import se.kodarkatten.casual.network.protocol.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by aleph on 2017-04-03.
 */
class CasualTransactionResourceCommitRequestMessageTest extends Specification
{
    @Shared
    def execution = UUID.randomUUID()

    @Shared
    def xid = XID.NULL_XID

    @Shared
    def resourceId = 12345

    @Shared
    def flags = Flag.of().setFlag(XAFlags.TMSUCCESS)
                         .setFlag(XAFlags.TMJOIN)

    def "Message creation"()
    {
        setup:
        when:
        def msg = CasualTransactionResourceCommitRequestMessage.of(execution, xid, resourceId, flags)
        then:
        msg.execution == execution
        msg.xid == xid
        msg.resourceId == resourceId
        msg.flags.flagValue == flags.flagValue
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE"()
    {
        setup:
        def requestMsg = CasualTransactionResourceCommitRequestMessage.of(execution, xid, resourceId, flags)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "Roundtrip message - force chunking"()
    {
        setup:
        def requestMsg = CasualTransactionResourceCommitRequestMessage.of(execution, xid, resourceId, flags)
        requestMsg.setMaxMessageSize(1)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalAsyncByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
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
        def requestMsg = CasualTransactionResourceCommitRequestMessage.of(execution, xid, resourceId, flags)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "Roundtrip message - force chunking, sync"()
    {
        setup:
        def requestMsg = CasualTransactionResourceCommitRequestMessage.of(execution, xid, resourceId, flags)
        requestMsg.setMaxMessageSize(1)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkWriter.write(sink, msg)
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        networkBytes.size() > 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

}
