package se.kodarkatten.casual.jca.service

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.ServiceReturn
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.*
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.*

import se.kodarkatten.casual.internal.network.NetworkConnection
import se.kodarkatten.casual.network.messages.domain.TransactionType
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl

import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.protocol.messages.domain.Service

import se.kodarkatten.casual.network.protocol.messages.exceptions.CasualProtocolException

import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.work.WorkManager
import java.util.concurrent.CompletableFuture

import static se.kodarkatten.casual.test.matchers.CasualNWMessageMatchers.matching
import static spock.util.matcher.HamcrestSupport.expect

class CasualServiceCallerTest extends Specification
{
    @Shared CasualServiceCaller instance
    @Shared CasualManagedConnection connection
    @Shared NetworkConnection networkConnection
    @Shared UUID executionId
    @Shared UUID domainId
    @Shared String domainName
    @Shared String serviceName
    @Shared JsonBuffer message
    @Shared CasualServiceCallRequestMessage expectedServiceRequest
    @Shared CasualNWMessageImpl<CasualServiceCallReplyMessage> serviceReply
    @Shared CasualNWMessageImpl<CasualServiceCallRequestMessage> actualServiceRequest
    @Shared CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> actualDomainDiscoveryRequest
    @Shared CasualDomainDiscoveryRequestMessage expectedDomainDiscoveryRequest
    @Shared CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> domainDiscoveryReplyFound
    @Shared CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> domainDiscoveryReplyNotFound
    @Shared mcf
    @Shared ra
    @Shared workManager

    def setup()
    {
        workManager = Mock(WorkManager)
        ra = new CasualResourceAdapter()
        ra.workManager = workManager
        mcf = Mock(CasualManagedConnectionFactory)
        networkConnection = Mock(NetworkConnection)
        connection = new CasualManagedConnection( mcf, null )
        connection.networkConnection =  networkConnection

        CasualResourceManager.getInstance().remove(XID.NULL_XID)
        connection.getXAResource().start( XID.NULL_XID, 0 )
        CasualResourceManager.getInstance().remove(XID.NULL_XID)

        instance = CasualServiceCaller.of( connection )

        initialiseParameters()
        initialiseExpectedRequests()
        initialiseReplies()
    }

    def initialiseParameters()
    {
        executionId = UUID.randomUUID()
        domainId = UUID.randomUUID()
        domainName = connection.getDomainName()
        serviceName = "echo"
        message = JsonBuffer.of( "{msg: \"hello echo service.\"}" )
    }

    def initialiseExpectedRequests()
    {
        expectedServiceRequest = CasualServiceCallRequestMessage.createBuilder()
                .setServiceBuffer(ServiceBuffer.of(message.getType(), message.getBytes()))
                .setServiceName(serviceName)
                .setXid( connection.getCurrentXid() )
                .build()
        expectedDomainDiscoveryRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setServiceNames([serviceName])
                .setDomainName(connection.getDomainName())
                .build()
    }

    def initialiseReplies()
    {
        serviceReply = createServiceCallReplyMessage( ErrorState.OK, TransactionState.TX_ACTIVE, message )
        domainDiscoveryReplyFound = createDomainDiscoveryReply(asServices([serviceName]))
        domainDiscoveryReplyNotFound = createDomainDiscoveryReply(asServices([]))
    }

    List<Service> asServices(List<String> serviceNames)
    {
        List<Service> l = new ArrayList<>()
        for(String s : serviceNames)
        {
            l.add(Service.of(s, '', TransactionType.AUTOMATIC))
        }
        return l
    }

    CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> createDomainDiscoveryReply(List<Service> services)
    {
        CasualNWMessageImpl.of(executionId,
                CasualDomainDiscoveryReplyMessage.of(executionId, domainId, domainName)
                                                 .setServices(services))
    }

    CasualNWMessageImpl<CasualServiceCallReplyMessage> createServiceCallReplyMessage(ErrorState errorState, TransactionState transactionState, JsonBuffer message )
    {
        CasualNWMessageImpl.of( executionId,
                CasualServiceCallReplyMessage.createBuilder()
                        .setExecution( executionId )
                        .setError( errorState)
                        .setTransactionState( transactionState )
                        .setXid( XID.NULL_XID )
                        .setServiceBuffer(ServiceBuffer.of(message))
                        .build()
        )
    }

    def "Tpcall service is available performs service call and returns result of service call."()
    {
        when:
        ServiceReturn<CasualBuffer> result = instance.tpcall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))

        then:
        result != null
        result.getServiceReturnState() == ServiceReturnState.TPSUCCESS

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return new CompletableFuture<>(serviceReply)
        }
        expect actualServiceRequest, matching( expectedServiceRequest )
    }

    def "Tpcall service not available returns TPNOENT"()
    {
        setup:
        serviceReply = createServiceCallReplyMessage( ErrorState.TPENOENT, TransactionState.ROLLBACK_ONLY, JsonBuffer.of( new ArrayList<byte[]>() ) )
        when:
        instance.tpcall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return new CompletableFuture<>(serviceReply)
        }

        expect actualServiceRequest, matching( expectedServiceRequest )
    }

    def "Tpcall service is available performs service call which fails, returns failure result."()
    {
        setup:
        serviceReply = createServiceCallReplyMessage( ErrorState.TPESVCFAIL, TransactionState.ROLLBACK_ONLY, JsonBuffer.of( new ArrayList<byte[]>() ) )

        when:
        ServiceReturn<CasualBuffer> result = instance.tpcall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))

        then:
        result != null
        result.getServiceReturnState() == ServiceReturnState.TPFAIL

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return new CompletableFuture<>(serviceReply)
        }

        expect actualServiceRequest, matching( expectedServiceRequest )
    }

    def "Tpacall service is available performs service call and returns result of service call."()
    {
        when:
        ServiceReturn<CasualBuffer> result = instance.tpacall( serviceName, message, Flag.of( AtmiFlags.NOFLAG)).get()

        then:
        noExceptionThrown()
        result != null
        result.getServiceReturnState() == ServiceReturnState.TPSUCCESS

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return new CompletableFuture<>(serviceReply)
        }

        expect actualServiceRequest, matching( expectedServiceRequest )
    }

    def 'tpacall fails'()
    {
        when:
        CompletableFuture<ServiceReturn<CasualBuffer>> result = instance.tpacall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))
        then:
        noExceptionThrown()
        result.isCompletedExceptionally()

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                CompletableFuture<ServiceReturn<CasualBuffer>> f = new CompletableFuture<>()
                f.completeExceptionally(new CasualProtocolException('oopsie'))
                return f
        }
    }

    def 'serviceExists'()
    {
        when:
        def r = instance.serviceExists(serviceName)
        then:
        noExceptionThrown()
        r == true
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                actualDomainDiscoveryRequest = input
                return new CompletableFuture<>(domainDiscoveryReplyFound)
        }
        expect actualDomainDiscoveryRequest, matching(expectedDomainDiscoveryRequest)
    }

    def 'serviceExists - not found'()
    {
        when:
        def r = instance.serviceExists(serviceName)
        then:
        noExceptionThrown()
        r == false
        1 * networkConnection.request(_) >> {
            CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> input ->
                actualDomainDiscoveryRequest = input
                return new CompletableFuture<>(domainDiscoveryReplyNotFound)
        }
        expect actualDomainDiscoveryRequest, matching(expectedDomainDiscoveryRequest)
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualServiceCaller" )
    }
}
