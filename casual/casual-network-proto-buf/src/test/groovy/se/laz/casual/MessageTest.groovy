package se.laz.casual

import se.laz.casual.network.messages.CasualDomainConnectRequest
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.messages.UUID4
import spock.lang.Specification

class MessageTest extends Specification
{
    def 'roundtrip CasualDomainConnectRequest to file and back'()
    {
        given:
        def messageType = CasualRequest.MessageType.DOMAIN_CONNECT_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID domainId = UUID.randomUUID()
        def domainName = "Casually"
        def protocolVersion = 1000L
        def tmpFile = File.createTempFile('CasualDomainConnectRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def domainConnectRequest = CasualDomainConnectRequest.newBuilder()
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

        def request = CasualRequest.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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
        def messageType = CasualRequest.MessageType.DOMAIN_CONNECT_REQUEST
        UUID corrid = UUID.randomUUID()
        UUID execution = UUID.randomUUID()
        UUID domainId = UUID.randomUUID()
        def domainName = "Casually"
        def protocolVersion = 1000L
        def services = ['Service A', 'Service B']
        def queues = ['A', 'B', 'C']
        def tmpFile = File.createTempFile('CasualDomainConnectRequest','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def domainDiscoveryRequest = CasualDomainDiscoveryRequest.newBuilder()
                .setExecution(UUID4.newBuilder()
                        .setMostSignificantBits(execution.getMostSignificantBits())
                        .setLeastSignificantBits(execution.getLeastSignificantBits())
                        .build())
                .setDomainId(UUID4.newBuilder()
                        .setMostSignificantBits(domainId.getMostSignificantBits())
                        .setLeastSignificantBits(domainId.getLeastSignificantBits())
                        .build())
                .setDomainName(domainName)
                .addAllServiceNames(services)
                .addAllQueueNames(queues)
                .build()

        def request = CasualRequest.newBuilder()
                .setMessageType(messageType)
                .setCorrelationId(UUID4.newBuilder()
                        .setMostSignificantBits(corrid.getMostSignificantBits())
                        .setLeastSignificantBits(corrid.getLeastSignificantBits())
                        .build())
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

}
