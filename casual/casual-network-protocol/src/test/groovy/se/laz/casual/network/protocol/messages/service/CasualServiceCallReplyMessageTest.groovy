/*
 * Copyright (c) 2017 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service

import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.xa.XID
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.ByteUtils
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

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
    @Shared
    def emptyServiceBuffer

    def setupSpec()
    {
        List<byte[]> l = new ArrayList<>()
        l.add([2,3,4] as byte[])
        serviceData = l
        serviceBuffer = ServiceBuffer.of(serviceType, serviceData)
        emptyServiceBuffer = ServiceBuffer.empty()
    }

    def "Message creation"()
    {
        setup:
        when:
        def msgBuilder = CasualServiceCallReplyMessage.createBuilder()
                                               .setExecution(execution)
                                               .setError(callError)
                                               .setUserSuppliedError(userError)
                                               .setTransactionState(transactionState)
                                               .setServiceBuffer(serviceBuffer)
                                               .setProtocolVersion(protocolVersion)
        if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion))
        {
           msgBuilder.setXid(nullXID)
        }
        def msg = msgBuilder.build()
        then:
        msg.getExecution() == execution
        msg.getError() == callError
        msg.getUserDefinedCode() == userError
        if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion))
        {
           msg.getXid() == nullXID
        }
        msg.getTransactionState() == transactionState
        msg.getServiceBuffer() == serviceBuffer
        msg.getServiceBuffer().payload == serviceBuffer.payload
        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        def requestMsgBuilder = CasualServiceCallReplyMessage.createBuilder()
                .setExecution(execution)
                .setError(callError)
                .setUserSuppliedError(userError)
                .setTransactionState(transactionState)
                .setServiceBuffer(serviceBuffer)
                .setProtocolVersion(protocolVersion)

        if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion))
        {
           requestMsgBuilder.setXid(nullXID)
        }
        def requestMsg = requestMsgBuilder.build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        resurrectedMsg.getMessage().getServiceBuffer().getPayload().size() == 1
        requestMsg.serviceBuffer.payload == resurrectedMsg.getMessage().getServiceBuffer().payload
        if(!ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion))
        {
           resurrectedMsg.getMessage().getXid() == nullXID
        }
        where:
        protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
    }

    def "Roundtrip with empty service buffer"()
    {
        setup:
        def requestMsg = CasualServiceCallReplyMessage.createBuilder()
                .setExecution(execution)
                .setError(ErrorState.TPESVCERR)
                .setUserSuppliedError(userError)
                .setXid(nullXID)
                .setTransactionState(TransactionState.ROLLBACK_ONLY)
                .setServiceBuffer(emptyServiceBuffer)
                .setProtocolVersion(ProtocolVersion.VERSION_1_2)
                .build()
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        requestMsg == resurrectedMsg.getMessage()
        resurrectedMsg.getMessage().getServiceBuffer().isEmpty()
        msg == resurrectedMsg
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
