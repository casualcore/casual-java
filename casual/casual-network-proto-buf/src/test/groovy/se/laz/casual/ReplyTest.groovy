package se.laz.casual

import com.google.protobuf.ByteString
import se.laz.casual.network.messages.CasualCommitReply
import se.laz.casual.network.messages.CasualDequeueReply
import se.laz.casual.network.messages.CasualDomainConnectReply
import se.laz.casual.network.messages.CasualDomainDiscoveryReply
import se.laz.casual.network.messages.CasualEnqueueReply
import se.laz.casual.network.messages.CasualPrepareReply
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualRollbackReply
import se.laz.casual.network.messages.CasualServiceCallReply
import se.laz.casual.network.messages.DequeueMessage
import se.laz.casual.network.messages.Queue
import se.laz.casual.network.messages.Service
import se.laz.casual.network.messages.State
import se.laz.casual.network.messages.TransactionState
import se.laz.casual.network.messages.UUID4
import se.laz.casual.network.messages.XID
import spock.lang.Specification

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
        def domainConnectReply = CasualDomainConnectReply.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setDomainId(UUID4.newBuilder()
                        .setMostSignificantBits(domainId.getMostSignificantBits())
                        .setLeastSignificantBits(domainId.getLeastSignificantBits())
                        .build())
                .setDomainName(domainName)
                .setProtocolVersion(protocolVersion)
                .build()

        def reply = CasualReply.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
        def domainDiscoveryReply = CasualDomainDiscoveryReply.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setDomainId(UUID4.newBuilder()
                        .setMostSignificantBits(domainId.getMostSignificantBits())
                        .setLeastSignificantBits(domainId.getLeastSignificantBits())
                        .build())
                .setDomainName(domainName)
                .addAllServices(createServices(services))
                .addAllQueues(createQueues(queues))
                .build()

        def reply = CasualReply.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
        def tmpFile = File.createTempFile('CasualServiceCallReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def serviceCallReply = CasualServiceCallReply.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setXid(XID.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setResult(0)
                .setUser(0)
                .setTransactionState(TransactionState.TX_ACTIVE)
                .setBufferTypeName('Bob')
                .setPayload(ByteString.copyFrom('Austin Texas'.getBytes()))
                .build()

        def reply = CasualReply.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
        def tmpFile = File.createTempFile('CasualPrepareReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def prepareReply = CasualPrepareReply.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setXid(XID.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setResourceManagerId(42)
                .setState(State.TPENOENT)
                .build()

        def reply = CasualReply.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
        def tmpFile = File.createTempFile('CasualCommitReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def commitReply = CasualCommitReply.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setXid(XID.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setResourceManagerId(42)
                .setState(State.OK)
                .build()

        def reply = CasualReply.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
        def tmpFile = File.createTempFile('CasualRollbackReply','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def rollbackReply = CasualRollbackReply.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setXid(XID.newBuilder()
                        .setXidFormat(0)
                        .setXidGtridLength(32)
                        .setXidBqualLength(10)
                        .setXidData(ByteString.copyFrom('a'.getBytes()))
                        .build())
                .setResourceManagerId(42)
                .setState(State.TPESVCERR)
                .build()

        def reply = CasualReply.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
        def enqueueReply = CasualEnqueueReply.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setMessageId(UUID4.newBuilder()
                        .setMostSignificantBits(msgId.getMostSignificantBits())
                        .setLeastSignificantBits(msgId.getLeastSignificantBits())
                        .build())
                .build()

        def reply = CasualReply.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
        def dequeueReply = CasualDequeueReply.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .addAllMessage(creatDequedMessages(['Hello world', 'Hello Mars', 'Hello Jupiter']))
                .build()

        def reply = CasualReply.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
             DequeueMessage msg = DequeueMessage.newBuilder()
                     .setId(UUID4.newBuilder()
                             .setMostSignificantBits(msgId.getMostSignificantBits())
                             .setLeastSignificantBits(msgId.getLeastSignificantBits())
                             .build())
                     .setProperties('abcd')
                     .setReplyQueue('redirected_here')
                     .setAvailableSince(1234)
                     .setType('custom type')
                     .setPayload(ByteString.copyFrom(it.getBytes()))
                     .setRedeliveredCount(1)
                     .setTimestamp(1234)
                     .build()
             messages << msg
         }
         return messages
     }
}
