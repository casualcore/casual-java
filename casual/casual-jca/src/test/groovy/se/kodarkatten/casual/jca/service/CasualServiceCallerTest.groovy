package se.kodarkatten.casual.jca.service

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.ServiceReturn
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.flags.*
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.jca.CasualManagedConnection
import se.kodarkatten.casual.jca.CasualManagedConnectionFactory
import se.kodarkatten.casual.jca.CasualResourceAdapter
import se.kodarkatten.casual.jca.CasualResourceManager
import se.kodarkatten.casual.jca.NetworkConnection
import se.kodarkatten.casual.network.connection.CasualConnectionException
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage
import se.kodarkatten.casual.network.messages.domain.Service
import se.kodarkatten.casual.network.messages.domain.TransactionType
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.messages.service.ServiceBuffer
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.work.Work
import javax.resource.spi.work.WorkException
import javax.resource.spi.work.WorkManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

import static se.kodarkatten.casual.jca.test.CasualNWMessageMatchers.matching
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
    @Shared CasualDomainDiscoveryRequestMessage expectedDiscoverRequest
    @Shared CasualServiceCallRequestMessage expectedServiceRequest
    @Shared CasualNWMessage<CasualDomainDiscoveryReplyMessage> discoveryReply
    @Shared CasualNWMessage<CasualServiceCallReplyMessage> serviceReply
    @Shared CasualNWMessage<CasualDomainDiscoveryRequestMessage> actualDiscoveryRequest
    @Shared CasualNWMessage<CasualServiceCallRequestMessage> actualServiceRequest
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

        CasualResourceManager.getInstance().remove(XID.of())
        connection.getXAResource().start( XID.of(), 0 )
        CasualResourceManager.getInstance().remove(XID.of())

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
        expectedDiscoverRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setDomainName( connection.getDomainName() )
                .setServiceNames(Arrays.asList(serviceName))
                .build()

        expectedServiceRequest = CasualServiceCallRequestMessage.createBuilder()
                .setServiceBuffer(ServiceBuffer.of(message.getType(), message.getBytes()))
                .setServiceName(serviceName)
                .setXid( connection.getCurrentXid() )
                .build()
    }

    def initialiseReplies()
    {
        discoveryReply = createDomainDiscoveryReplyMessage( serviceName )

        serviceReply = createServiceCallReplyMessage( ErrorState.OK, TransactionState.TX_ACTIVE, message )
    }

    CasualNWMessage<CasualDomainDiscoveryReplyMessage> createDomainDiscoveryReplyMessage( String...services )
    {
        CasualDomainDiscoveryReplyMessage msg = CasualDomainDiscoveryReplyMessage.of( executionId, domainId, domainName )
        List<Service> available = new ArrayList<>()
        for( String s: services )
        {
            available.add(Service.of(s, "", TransactionType.ATOMIC))
        }
        msg.setServices( available )
        return CasualNWMessage.of( executionId, msg )
    }

    CasualNWMessage<CasualServiceCallReplyMessage> createServiceCallReplyMessage( ErrorState errorState, TransactionState transactionState, JsonBuffer message )
    {
        CasualNWMessage.of( executionId,
                CasualServiceCallReplyMessage.createBuilder()
                        .setExecution( executionId )
                        .setError( errorState)
                        .setTransactionState( transactionState )
                        .setXid( XID.of() )
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

        1 * networkConnection.requestReply( _ ) >> {
                CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                    actualDiscoveryRequest = input
                    return discoveryReply
        }
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return serviceReply
        }

        expect actualDiscoveryRequest, matching( expectedDiscoverRequest )

        expect actualServiceRequest, matching( expectedServiceRequest )
    }

    def "Tpcall service not available throws CasualConnectionException no further network calls."()
    {
        setup:
        discoveryReply = createDomainDiscoveryReplyMessage( "other" )

        when:
        instance.tpcall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))

        then:
        thrown CasualConnectionException

        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return discoveryReply
        }

        expect actualDiscoveryRequest, matching( expectedDiscoverRequest )
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

        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return discoveryReply
        }
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return serviceReply
        }

        expect actualDiscoveryRequest, matching( expectedDiscoverRequest )

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

        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualDomainDiscoveryRequestMessage> input ->
                actualDiscoveryRequest = input
                return discoveryReply
        }
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return serviceReply
        }

        1 * workManager.scheduleWork(_) >> {
            Work w ->
                Executors.callable(w).call()
        }

        1 * mcf.getResourceAdapter() >> ra

        expect actualDiscoveryRequest, matching( expectedDiscoverRequest )
        expect actualServiceRequest, matching( expectedServiceRequest )
    }

    def "Tpacall service scheduling of work fails"()
    {
        when:
        CompletableFuture<ServiceReturn<CasualBuffer>> result = instance.tpacall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))

        then:
        noExceptionThrown()
        result.isCompletedExceptionally()
        1 * workManager.scheduleWork(_) >> {
            throw new WorkException('oops')
        }
        1 * mcf.getResourceAdapter() >> ra
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualServiceCaller" )
    }
}
