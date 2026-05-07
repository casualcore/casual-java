/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.service

import se.laz.casual.api.buffer.CasualHeaders
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.api.xa.XID
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

import static se.laz.casual.network.ProtocolVersion.VERSION_1_3
import static se.laz.casual.network.ProtocolVersion.VERSION_1_5

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
    @Shared
    List<String> rawHeaders = ["a:foo","b:bar","c:baz"]
    @Shared
    CasualHeaders headers

    def setupSpec()
    {
        List<byte[]> l = new ArrayList<>()
        l.add([2,3,4] as byte[])
        serviceData = l
        serviceBuffer = ServiceBuffer.of(serviceType, serviceData)
        emptyServiceBuffer = ServiceBuffer.empty()

        headers = CasualHeaders.newBuilder().addAll( rawHeaders ).build(  )
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
        if(protocolVersion.isLessThan( VERSION_1_3 ) )
        {
           msgBuilder.setXid(nullXID)
        }
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ) )
        {
            msgBuilder.setHeaders( headers )
        }
        def msg = msgBuilder.build()
        then:
        msg.getExecution() == execution
        msg.getError() == callError
        msg.getUserDefinedCode() == userError
        if(protocolVersion.isLessThan( VERSION_1_3 ) )
        {
           msg.getXid() == nullXID
        }
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ) )
        {
            msg.getHeaders() == headers
        }
        msg.getTransactionState() == transactionState
        msg.getServiceBuffer() == serviceBuffer
        msg.getServiceBuffer().payload == serviceBuffer.payload

        msg.getType(  ) == type

        where:
        protocolVersion | type
        ProtocolVersion.VERSION_1_0 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_1 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_2 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_3 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_3
        ProtocolVersion.VERSION_1_4 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_3
        ProtocolVersion.VERSION_1_5 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_5
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

        if(protocolVersion.isLessThan( VERSION_1_3 ))
        {
           requestMsgBuilder.setXid(nullXID)
        }
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ) )
        {
            requestMsgBuilder.setHeaders( headers )
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
        if(protocolVersion.isLessThan( VERSION_1_3 ))
        {
           resurrectedMsg.getMessage().getXid() == nullXID
        }
        if(protocolVersion.isGreaterThanOrEqualTo( VERSION_1_5 ))
        {
            resurrectedMsg.getMessage(  ).getHeaders() == headers
        }

        msg.getType(  ) == type

        where:
        protocolVersion | type
        ProtocolVersion.VERSION_1_0 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_1 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_2 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_3 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_3
        ProtocolVersion.VERSION_1_4 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_3
        ProtocolVersion.VERSION_1_5 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_5
    }

    def "Roundtrip with empty service buffer"()
    {
        setup:
        def requestMsgBuilder = CasualServiceCallReplyMessage.createBuilder()
                .setExecution(execution)
                .setError(ErrorState.TPESVCERR)
                .setUserSuppliedError(userError)
                .setTransactionState(TransactionState.ROLLBACK_ONLY)
                .setServiceBuffer(emptyServiceBuffer)
                .setProtocolVersion(protocolVersion)
        if(protocolVersion.isLessThan( VERSION_1_3 ))
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
        resurrectedMsg.getMessage().getServiceBuffer().isEmpty()
        msg == resurrectedMsg

        msg.getType(  ) == type

        where:
        protocolVersion | type
        ProtocolVersion.VERSION_1_0 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_1 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_2 | CasualNWMessageType.SERVICE_CALL_REPLY
        ProtocolVersion.VERSION_1_3 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_3
        ProtocolVersion.VERSION_1_4 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_3
        ProtocolVersion.VERSION_1_5 | CasualNWMessageType.SERVICE_CALL_REPLY_V_1_5
    }

}
