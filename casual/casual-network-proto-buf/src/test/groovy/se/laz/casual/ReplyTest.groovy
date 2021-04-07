package se.laz.casual

import se.laz.casual.api.flags.ErrorState
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.DequeueMessage
import se.laz.casual.network.messages.Queue
import se.laz.casual.network.messages.Service
import se.laz.casual.network.messages.TransactionState
import se.laz.casual.network.messages.XAReturnCode
import se.laz.casual.network.messages.XID
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneOffset

class ReplyTest extends Specification
{
    def 'roundtrip CasualDomainConnectReply to file and back'()
    {
        given:
        def messageType = CasualReply.MessageType.DOMAIN_CONNECT_REPLY
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID domainId = UUID.randomUUID()
        def domainName = "Casually"
        def protocolVersion = 2000L
        def tmpFile = File.createTempFile('CasualDomainConnectReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def domainConnectReply = MessageCreator.createCasualDomainConnectReply(execution, domainId, domainName, protocolVersion)
        def reply = MessageCreator.createReplyBuilder(messageType, corrid)
                                  .setDomainConnect(domainConnectReply)
                                  .build()
        reply.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualReply.parseFrom(fs)
        fs.close()
        then:
        restored == reply
        restored.hasDomainConnect()
        restored.getDomainConnect() == domainConnectReply
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualDomainDiscoveryReply to file and back'()
    {
        given:
        def messageType = CasualReply.MessageType.DOMAIN_DISCOVERY_REPLY
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID domainId = UUID.randomUUID()
        def domainName = "Casually"
        def services = ['Service A', 'Service B']
        def queues = ['A', 'B', 'C']
        def tmpFile = File.createTempFile('CasualDomainDiscoveryReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def domainDiscoveryReply = MessageCreator.createCasualDomainDiscoveryReply(execution, domainId, domainName,
                                                                                   Optional.of(createServices(services)),
                                                                                   Optional.of(createQueues(queues)))
        def reply = MessageCreator.createReplyBuilder(messageType, corrid)
                                  .setDomainDiscovery(domainDiscoveryReply)
                                  .build()
        reply.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualReply.parseFrom(fs)
        fs.close()
        then:
        restored == reply
        restored.hasDomainDiscovery()
        restored.getDomainDiscovery() == domainDiscoveryReply
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualServiceCallReply to file and back'()
    {
        given:
        def messageType = CasualReply.MessageType.SERVICE_CALL_REPLY
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def result = 0
        def user = 0
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        def tmpFile = File.createTempFile('CasualServiceCallReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def serviceCallReply = MessageCreator.createCasualServiceCallReply(execution, ErrorState.unmarshal(result), user, xid, TransactionState.TX_ACTIVE, 'Fast type', 'asdf'.getBytes())
        def reply = MessageCreator.createReplyBuilder(messageType, corrid)
                                  .setServiceCall(serviceCallReply)
                                  .build()
        reply.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualReply.parseFrom(fs)
        fs.close()
        then:
        restored == reply
        restored.hasServiceCall()
        restored.getServiceCall() == serviceCallReply
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualPrepareReply to file and back'()
    {
        given:
        def messageType = CasualReply.MessageType.PREPARE_REPLY
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        def resourceManagerId = 42
        def tmpFile = File.createTempFile('CasualPrepareReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def prepareReply = MessageCreator.createCasualPrepareReply(execution, xid, resourceManagerId, XAReturnCode.XA_OK)
        def reply = MessageCreator.createReplyBuilder(messageType, corrid)
                                  .setPrepare(prepareReply)
                                  .build()
        reply.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualReply.parseFrom(fs)
        fs.close()
        then:
        restored == reply
        restored.hasPrepare()
        restored.getPrepare() == prepareReply
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualCommitReply to file and back'()
    {
        given:
        def messageType = CasualReply.MessageType.COMMIT_REPLY
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        def resourceManagerId = 42
        def tmpFile = File.createTempFile('CasualCommitReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def commitReply = MessageCreator.createCasualCommitReply(execution, xid, resourceManagerId, XAReturnCode.XA_OK)
        def reply = MessageCreator.createReplyBuilder(messageType, corrid)
                                  .setCommit(commitReply)
                                  .build()
        reply.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualReply.parseFrom(fs)
        fs.close()
        then:
        restored == reply
        restored.hasCommit()
        restored.getCommit() == commitReply
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualRollbackReply to file and back'()
    {
        given:
        def messageType = CasualReply.MessageType.ROLLBACK_REPLY
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def gtridLength = 2
        def bqualLength = 2
        XID xid = MessageCreator.createXID(gtridLength, bqualLength, 42, 'asdf'.getBytes())
        def resourceManagerId = 42
        def tmpFile = File.createTempFile('CasualRollbackReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def rollbackReply = MessageCreator.createCasualRollbackReply(execution, xid, resourceManagerId, XAReturnCode.XA_OK)
        def reply = MessageCreator.createReplyBuilder(messageType, corrid)
                                  .setRollback(rollbackReply)
                                  .build()
        reply.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualReply.parseFrom(fs)
        fs.close()
        then:
        restored == reply
        restored.hasRollback()
        restored.getRollback() == rollbackReply
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualEnqueueReply to file and back'()
    {
        given:
        def messageType = CasualReply.MessageType.ENQUEUE_REPLY
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID msgId = UUID.randomUUID()
        def tmpFile = File.createTempFile('CasualEnqueueReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def enqueueReply = MessageCreator.createCasualEnqueueReply(execution, msgId)
        def reply = MessageCreator.createReplyBuilder(messageType, corrid)
                                  .setEnqueue(enqueueReply)
                                  .build()
        reply.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualReply.parseFrom(fs)
        fs.close()
        then:
        restored == reply
        restored.hasEnqueue()
        restored.getEnqueue() == enqueueReply
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    def 'roundtrip CasualDequeueReply to file and back'()
    {
        given:
        def messageType = CasualReply.MessageType.DEQUEUE_REPLY
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        def tmpFile = File.createTempFile('CasualDequeueReply','.bin')

        FileOutputStream os = new FileOutputStream(tmpFile)
        def dequeueReply = MessageCreator.createCasualDequeueReply(execution, creatDequedMessages(['Hello world', 'Hello Mars', 'Hello Jupiter']))
        def reply = MessageCreator.createReplyBuilder(messageType, corrid)
                                  .setDequeue(dequeueReply)
                                  .build()
        reply.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        def restored = CasualReply.parseFrom(fs)
        fs.close()
        then:
        restored == reply
        restored.hasDequeue()
        restored.getDequeue() == dequeueReply
        when:
        UUID restoredCorrid = new UUID(restored.getCorrelationId().getMostSignificantBits(), restored.getCorrelationId().getLeastSignificantBits())
        then:
        restoredCorrid == corrid
    }

    List<Service> createServices(List<String> serviceNames)
    {
        List<Service> services = []
        serviceNames.each{
            Service service = Service.newBuilder()
                    .setName(it)
                    .setCategory('A nice category')
                    .setTimeout(42)
                    .setHops(2)
                    .build()
            services << service
        }
        return services
    }

    List<Queue> createQueues(List<String> queueNames)
    {
        List<Queue> queues = []
        queueNames.each{
            Queue q = Queue.newBuilder()
                    .setName(it)
                    .setRetries(1)
                    .build()
            queues << q
        }
        return queues
    }

    List<DequeueMessage> creatDequedMessages(List<String> content)
    {
        UUID msgId = UUID.randomUUID()
        List<DequeueMessage> messages = []
        content.each{
            DequeueMessage msg = MessageCreator.createDequeueMessage(msgId, Optional.empty(), Optional.empty(),
                    LocalDateTime.now().toInstant(ZoneOffset.UTC), 'custom type',
                    it.getBytes(), 0,
                    LocalDateTime.now().minusHours(5).toInstant(ZoneOffset.UTC))
            messages << msg
        }
        return messages
    }
}
