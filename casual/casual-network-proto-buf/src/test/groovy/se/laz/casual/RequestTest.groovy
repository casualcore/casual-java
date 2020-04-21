package se.laz.casual

import se.laz.casual.api.buffer.CasualBufferType
import se.laz.casual.api.buffer.type.CStringBuffer
import se.laz.casual.api.flags.XAFlags
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.messages.Selector
import se.laz.casual.network.messages.XID
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneOffset

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
        def protocolVersion = [1000L, 2000L]
        def tmpFile = File.createTempFile('CasualDomainConnectRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def domainConnectRequest = MessageCreator.createCasualDomainConnectRequest(execution,
                                                                                   domainId,
                                                                                   domainName,
                                                                                   protocolVersion)
        def request = MessageCreator.createRequestBuilder(messageType, corrid)
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
        def domainDiscoveryRequest = MessageCreator.createCasualDomainDiscoveryRequest(execution,
                                                                                       domainId,
                                                                                       domainName,
                                                                                       Optional.of(services),
                                                                                       Optional.of(queues))
        def request = MessageCreator.createRequestBuilder(messageType, corrid)
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
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        CStringBuffer buffer = CStringBuffer.of('Hello world')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def serviceCallRequest = MessageCreator.createCasualServiceCallRequest(execution,
                                                                               serviceName, 0L, Optional.empty(),
                                                                               xid,
                                                                               XAFlags.TMNOFLAGS.value,
                                                                               CasualBufferType.CSTRING.name,
                                                                               buffer.getBytes().get(0))
        def request = MessageCreator.createRequestBuilder(messageType, corrid)
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
        def resourceManagerId = 42
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        def tmpFile = File.createTempFile('CasualPrepareRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def prepareRequest = MessageCreator.createCasualPrepareRequest(execution, xid, resourceManagerId, XAFlags.TMNOFLAGS.value)
        def request =  MessageCreator.createRequestBuilder(messageType, corrid)
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
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        def resourceManagerId = 42
        def tmpFile = File.createTempFile('CasualCommitRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def commitRequest = MessageCreator.createCasualCommitRequest(execution, xid, resourceManagerId, XAFlags.TMNOFLAGS.value)
        def request = MessageCreator.createRequestBuilder(messageType, corrid)
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
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        def resourceManagerId = 42
        def tmpFile = File.createTempFile('CasualRollbackRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def rollbackRequest = MessageCreator.createCasualRollbackRequest(execution, xid, resourceManagerId, XAFlags.TMNOFLAGS.value)
        def request = MessageCreator.createRequestBuilder(messageType, corrid)
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
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        def tmpFile = File.createTempFile('CasualEnqueueRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def enqueueRequest = MessageCreator.createCasualEnqueueRequest(execution, 'Astrakan', xid,
                                                                       MessageCreator.createQueueMessage(msgId,Optional.empty(), Optional.empty(),
                                                                                                         LocalDateTime.now().toInstant(ZoneOffset.UTC),
                                                                                                    'Nice Type', 'Hello world'.getBytes()))
        def request = MessageCreator.createRequestBuilder(messageType, corrid)
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
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        Selector selector = MessageCreator.createSelector(msgId, Optional.empty())
        def block = true
        def tmpFile = File.createTempFile('CasualDequeueRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def dequeueRequest = MessageCreator.createCasualDequeueRequest(execution, 'Astrakan', xid, selector, block)
        def request = MessageCreator.createRequestBuilder(messageType, corrid)
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
