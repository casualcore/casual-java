package se.laz.casual

import com.google.protobuf.ByteString
import se.laz.casual.api.buffer.CasualBufferType
import se.laz.casual.api.buffer.type.CStringBuffer
import se.laz.casual.api.flags.XAFlags
import se.laz.casual.api.xa.XID
import se.laz.casual.api.xa.XIDFormatType
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualCommitRequest
import se.laz.casual.network.messages.CasualDequeueRequest
import se.laz.casual.network.messages.CasualDomainConnectRequest
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest
import se.laz.casual.network.messages.CasualEnqueueRequest
import se.laz.casual.network.messages.CasualPrepareRequest
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.messages.CasualRollbackRequest
import se.laz.casual.network.messages.CasualServiceCallRequest
import se.laz.casual.network.messages.QueueMessage
import se.laz.casual.network.messages.Selector
import se.laz.casual.network.messages.UUID4
import se.laz.casual.network.messages.XIDGRPC
import spock.lang.Specification

import javax.transaction.xa.Xid

class RequestTest extends Specification
{
    def 'roundtrip CasualDomainConnectRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.DOMAIN_CONNECT_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID domainId = UUID.randomUUID()
        def domainName = "Casually"
        def protocolVersion = 2000L
        def tmpFile = File.createTempFile('CasualDomainConnectRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def domainConnectRequest = MessageCreator.createCasualDomainConnectRequest(MessageCreator.toUUID4(execution),
                                                                                   MessageCreator.toUUID4(domainId),
                                                                                   domainName,
                                                                                   protocolVersion)
        def request = MessageCreator.createRequestBuilder(messageType, MessageCreator.toUUID4(corrid))
                                    .setDomainConnect(domainConnectRequest)
                                    .build()
        request.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualRequest.parseFrom(fs)
        fs.close()
        then:
        restored == request
        restored.hasDomainConnect()
        restored.getDomainConnect() == domainConnectRequest
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualDomainDiscoveryRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.DOMAIN_DISCOVERY_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID domainId = UUID.randomUUID()
        def domainName = "Casually"
        def services = ['Service A', 'Service B']
        def queues = ['A', 'B', 'C']
        def tmpFile = File.createTempFile('CasualDomainDiscoveryRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def domainDiscoveryRequest = MessageCreator.createCasualDomainDiscoveryRequest(MessageCreator.toUUID4(execution),
                                                                                       MessageCreator.toUUID4(domainId),
                                                                                       domainName,
                                                                                       Optional.of(services),
                                                                                       Optional.of(queues))
        def request = MessageCreator.createRequestBuilder(messageType, MessageCreator.toUUID4(corrid))
                                    .setDomainDiscovery(domainDiscoveryRequest)
                                    .build()

        request.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualRequest.parseFrom(fs)
        fs.close()
        then:
        restored == request
        restored.hasDomainDiscovery()
        restored.getDomainDiscovery() == domainDiscoveryRequest
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualServiceCallRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.SERVICE_CALL_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def tmpFile = File.createTempFile('CasualServiceCallRequest','.bin')
        def serviceName = 'Welcome to the Jungle'
        byte[] globalTransactionId = 'asdf'.getBytes()
        byte[] branchQualifier = 'qwerty'.getBytes()
        Xid xid = XID.of(globalTransactionId, branchQualifier, 42)
        CStringBuffer buffer = CStringBuffer.of('Hello world')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def serviceCallRequest = MessageCreator.createCasualServiceCallRequest(MessageCreator.toUUID4(execution),
                                                                               serviceName, 0L, Optional.empty(),
                                                                               MessageCreator.toXIDGRPC(xid),
                                                                               XAFlags.TMNOFLAGS.value,
                                                                               CasualBufferType.CSTRING.name,
                                                                               buffer.getBytes().get(0))
        def request = MessageCreator.createRequestBuilder(messageType, MessageCreator.toUUID4(corrid))
                                    .setServiceCall(serviceCallRequest)
                                    .build()
        request.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualRequest.parseFrom(fs)
        fs.close()
        then:
        restored == request
        restored.hasServiceCall()
        restored.getServiceCall() == serviceCallRequest
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualPrepareRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.PREPARE_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def tmpFile = File.createTempFile('CasualPrepareRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def prepareRequest = CasualPrepareRequest.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setXid(XIDGRPC.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setResourceManagerId(42)
                .setXaFlags(99)
                .build()

        def request = CasualRequest.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
                .setPrepare(prepareRequest)
                .build()
        request.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualRequest.parseFrom(fs)
        fs.close()
        then:
        restored == request
        restored.hasPrepare()
        restored.getPrepare() == prepareRequest
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualCommitRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.COMMIT_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def tmpFile = File.createTempFile('CasualCommitRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def commitRequest = CasualCommitRequest.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setXid(XIDGRPC.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setResourceManagerId(42)
                .setXaFlags(99)
                .build()

        def request = CasualRequest.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
                .setCommit(commitRequest)
                .build()
        request.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualRequest.parseFrom(fs)
        fs.close()
        then:
        restored == request
        restored.hasCommit()
        restored.getCommit() == commitRequest
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualRollbackRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.ROLLBACK_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def tmpFile = File.createTempFile('CasualRollbackRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def rollbackRequest = CasualRollbackRequest.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setXid(XIDGRPC.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setResourceManagerId(42)
                .setXaFlags(99)
                .build()

        def request = CasualRequest.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
                .setRollback(rollbackRequest)
                .build()
        request.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualRequest.parseFrom(fs)
        fs.close()
        then:
        restored == request
        restored.hasRollback()
        restored.getRollback() == rollbackRequest
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualEnqueueRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.ENQUEUE_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID msgId = UUID.randomUUID()
        def tmpFile = File.createTempFile('CasualEnqueueRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def enqueueRequest = CasualEnqueueRequest.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setQueueName('Astrakan')
                .setXid(XIDGRPC.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setMessage(QueueMessage.newBuilder()
                        .setId(UUID4.newBuilder()
                                .setMostSignificantBits(msgId.getMostSignificantBits())
                                .setLeastSignificantBits(msgId.getLeastSignificantBits())
                                .build())
                        .setProperties('No properties')
                        .setReplyQueue('here')
                        .setAvailableSince(72)
                        .setType('nice type')
                        .setPayload(ByteString.copyFrom('Hello world'.getBytes()))
                        .build())
                .build()

        def request = CasualRequest.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
                .setEnqueue(enqueueRequest)
                .build()
        request.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualRequest.parseFrom(fs)
        fs.close()
        then:
        restored == request
        restored.hasEnqueue()
        restored.getEnqueue() == enqueueRequest
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualDequeueRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.DEQUEUE_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID msgId = UUID.randomUUID()
        def tmpFile = File.createTempFile('CasualDequeueRequest','.bin')

        FileOutputStream os = new FileOutputStream(tmpFile)
        def dequeueRequest = CasualDequeueRequest.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setQueueName('Astrakan')
                .setXid(XIDGRPC.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setSelector(Selector.newBuilder()
                        .setId(UUID4.newBuilder()
                                .setMostSignificantBits(msgId.getMostSignificantBits())
                                .setLeastSignificantBits(msgId.getLeastSignificantBits())
                                .build())
                        .setProperties('well hello there')
                        .build())
                .build()

        def request = CasualRequest.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
                .setDequeue(dequeueRequest)
                .build()
        request.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualRequest.parseFrom(fs)
        fs.close()
        then:
        restored == request
        restored.hasDequeue()
        restored.getDequeue() == dequeueRequest
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }
}
