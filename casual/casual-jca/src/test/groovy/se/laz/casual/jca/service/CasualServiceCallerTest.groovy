/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.service

import jakarta.resource.spi.work.WorkManager
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ServiceReturn
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.ServiceReturnState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import se.laz.casual.api.xa.XID
import se.laz.casual.config.json.Domain
import se.laz.casual.event.Order
import se.laz.casual.event.ServiceCallEvent
import se.laz.casual.event.ServiceCallEventPublisher
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.CasualResourceAdapter
import se.laz.casual.jca.CasualResourceManager
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.messages.domain.TransactionType
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.domain.Service
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static se.laz.casual.test.matchers.CasualNWMessageMatchers.matching
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
    @Shared ServiceCallEventPublisher serviceCallEventPublisher

    def setup()
    {
        ra = new CasualResourceAdapter()
        workManager = Mock(WorkManager)
        ra.workManager = workManager
        mcf = Mock(CasualManagedConnectionFactory)
        networkConnection = Mock(NetworkConnection)
        connection = new CasualManagedConnection( mcf )
        connection.networkConnection =  networkConnection

        CasualResourceManager.getInstance().remove(XID.NULL_XID)
        connection.getXAResource().start( XID.NULL_XID, 0 )
        CasualResourceManager.getInstance().remove(XID.NULL_XID)

        instance = CasualServiceCaller.of( connection )
        serviceCallEventPublisher = Mock(ServiceCallEventPublisher)
        instance.setEventPublisher(serviceCallEventPublisher)

        initialiseParameters()
        initialiseExpectedRequests()
        initialiseReplies()
    }

    def initialiseParameters()
    {
        executionId = UUID.randomUUID()
        domainId = UUID.randomUUID()
        domainName = Domain.getName()
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
                .setDomainName(Domain.getName())
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

    CasualNWMessageImpl<CasualServiceCallReplyMessage> createServiceCallReplyMessageError(ErrorState errorState, TransactionState transactionState)
    {
        CasualNWMessageImpl.of( executionId,
                CasualServiceCallReplyMessage.createBuilder()
                        .setExecution( executionId )
                        .setError( errorState)
                        .setTransactionState( transactionState )
                        .setXid( XID.NULL_XID )
                        .setServiceBuffer(ServiceBuffer.empty())
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
                return CompletableFuture.completedFuture(serviceReply)
        }
        expect actualServiceRequest, matching( expectedServiceRequest )
        1 * serviceCallEventPublisher.post(_ as ServiceCallEvent) >> { ServiceCallEvent event ->
           event.getService() == serviceName
           event.getCode() == ErrorState.OK.name()
           event.getOrder() == Order.CONCURRENT.value
        }
    }

    def "Tpcall service not available returns TPNOENT"()
    {
        setup:
        serviceReply = createServiceCallReplyMessageError( ErrorState.TPENOENT, TransactionState.ROLLBACK_ONLY)
        when:
        instance.tpcall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return CompletableFuture.completedFuture(serviceReply)
        }

        expect actualServiceRequest, matching( expectedServiceRequest )
        1 * serviceCallEventPublisher.post(_ as ServiceCallEvent) >> { ServiceCallEvent event ->
           event.getService() == serviceName
           event.getCode() == ErrorState.TPENOENT.name()
           event.getOrder() == Order.CONCURRENT.value
        }
    }

    def "Tpcall service is available performs service call which fails, returns failure result."()
    {
        setup:
        serviceReply = createServiceCallReplyMessageError( ErrorState.TPESVCFAIL, TransactionState.ROLLBACK_ONLY)

        when:
        ServiceReturn<CasualBuffer> result = instance.tpcall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))

        then:
        result != null
        result.getServiceReturnState() == ServiceReturnState.TPFAIL

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return CompletableFuture.completedFuture(serviceReply)
        }

        expect actualServiceRequest, matching( expectedServiceRequest )
        1 * serviceCallEventPublisher.post(_ as ServiceCallEvent) >> { ServiceCallEvent event ->
           event.getService() == serviceName
           event.getCode() == ErrorState.TPESVCFAIL.name()
           event.getOrder() == Order.CONCURRENT.value
        }
    }

    def "Tpcall with TPNOREPLY - exceptional"()
    {
       when:
       instance.tpcall( serviceName, message, Flag.of( AtmiFlags.TPNOREPLY))
       then:
       def e = thrown(CasualConnectionException)
       e.cause.class == CasualProtocolException.class
       0 * serviceCallEventPublisher.post(_ as ServiceCallEvent)
    }

    def "Tpacall service is available performs service call and returns result of service call."()
    {
        when:
        Optional<ServiceReturn<CasualBuffer>> result = instance.tpacall( serviceName, message, Flag.of( AtmiFlags.NOFLAG)).get()

        then:
        noExceptionThrown()
        result != null
        result.isPresent()
        result.get().getServiceReturnState() == ServiceReturnState.TPSUCCESS

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualServiceCallRequestMessage> input ->
                actualServiceRequest = input
                return CompletableFuture.completedFuture(serviceReply)
        }

        expect actualServiceRequest, matching( expectedServiceRequest )
        1 * serviceCallEventPublisher.post(_ as ServiceCallEvent) >> { ServiceCallEvent event ->
           event.getService() == serviceName
           event.getCode() == ErrorState.TPESVCFAIL.name()
           event.getOrder() == Order.CONCURRENT.value
        }
    }

   def "Tpacall service is available, flags are TPNOREPLY but missing TPNOTRAN"()
   {
      when:
      instance.tpacall( serviceName, message, Flag.of( AtmiFlags.TPNOREPLY)).get()

      then:
      thrown(CasualProtocolException)
      0 * serviceCallEventPublisher.post(_ as ServiceCallEvent)
   }

   def "Tpacall service is available, flags are TPNOREPLY and TPNOTRAN"()
   {
      when:
      Optional<ServiceReturn<CasualBuffer>> result = instance.tpacall( serviceName, message, Flag.of( AtmiFlags.TPNOREPLY).setFlag (AtmiFlags.TPNOTRAN)).get()

      then:
      noExceptionThrown()
      result != null
      result.isPresent() == false
      1 * serviceCallEventPublisher.post(_ as ServiceCallEvent) >> { ServiceCallEvent event ->
         event.getService() == serviceName
         event.getCode() == ErrorState.TPESVCFAIL.name()
         event.getOrder() == Order.CONCURRENT.value
      }
   }

    def 'tpacall fails'()
    {
        when:
        CompletableFuture<Optional<ServiceReturn<CasualBuffer>>> result = instance.tpacall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))
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
        0 * serviceCallEventPublisher.post(_ as ServiceCallEvent)
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
                return CompletableFuture.completedFuture(domainDiscoveryReplyFound)
        }
        expect actualDomainDiscoveryRequest, matching(expectedDomainDiscoveryRequest)
        0 * serviceCallEventPublisher.post(_ as ServiceCallEvent)
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
                return CompletableFuture.completedFuture(domainDiscoveryReplyNotFound)
        }
        expect actualDomainDiscoveryRequest, matching(expectedDomainDiscoveryRequest)
        0 * serviceCallEventPublisher.post(_ as ServiceCallEvent)
    }

    def 'tpcall fails, exception is wrapped in CasualConnectionException'()
    {
        when:
        instance.tpcall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))
        then:
        thrown(CasualConnectionException)
        1 * networkConnection.request( _ ) >> {
            CompletableFuture<ServiceReturn<CasualBuffer>> f = new CompletableFuture<>()
            f.completeExceptionally(new CasualProtocolException('oopsie'))
            return f
        }
        0 * serviceCallEventPublisher.post(_ as ServiceCallEvent)
    }

    def 'service exists fails - exception is wrapped in CasualConnectionException'()
    {
        when:
        instance.serviceExists('foo')
        then:
        thrown(CasualConnectionException)
        1 * networkConnection.request( _ ) >> {
            CompletableFuture<ServiceReturn<CasualBuffer>> f = new CompletableFuture<>()
            f.completeExceptionally(new CasualProtocolException('oopsie'))
            return f
        }
        0 * serviceCallEventPublisher.post(_ as ServiceCallEvent)
    }


    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualServiceCaller" )
    }
}
