/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.service

import com.google.protobuf.ByteString
import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ServiceReturn
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.ServiceReturnState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import se.laz.casual.api.xa.XID
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.jca.CasualManagedConnection
import se.laz.casual.jca.CasualManagedConnectionFactory
import se.laz.casual.jca.CasualResourceAdapter
import se.laz.casual.jca.CasualResourceManager
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualDomainDiscoveryReply
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.messages.CasualServiceCallReply
import se.laz.casual.network.messages.CasualServiceCallRequest
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.work.WorkManager
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
    @Shared CasualServiceCallRequest expectedServiceRequest
    @Shared CasualReply serviceReply
    @Shared CasualRequest actualServiceRequest
    @Shared CasualRequest actualDomainDiscoveryRequest
    @Shared CasualDomainDiscoveryRequest expectedDomainDiscoveryRequest
    @Shared CasualReply domainDiscoveryReplyFound
    @Shared CasualReply domainDiscoveryReplyNotFound
    @Shared mcf
    @Shared ra
    @Shared workManager
    @Shared json = '{"hello":"world"}'

    def setup()
    {
        workManager = Mock(WorkManager)
        ra = new CasualResourceAdapter()
        ra.workManager = workManager
        mcf = Mock(CasualManagedConnectionFactory)
        networkConnection = Mock(NetworkConnection)
        connection = new CasualManagedConnection( mcf )
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
        expectedServiceRequest = CasualServiceCallRequest.newBuilder()
                .setBufferTypeName(message.getType())
                .setPayload(ByteString.copyFrom(message.getBytes().get(0)))
                .setServiceName(serviceName)
                .setXid(MessageCreator.toXID(connection.getCurrentXid()))
                .build()

        expectedDomainDiscoveryRequest = CasualDomainDiscoveryRequest.newBuilder()
                .addAllServiceNames([serviceName])
                .setDomainName(connection.getDomainName())
                .build()
    }

    def initialiseReplies()
    {
        serviceReply = createServiceCallReplyMessage( ErrorState.OK, TransactionState.TX_ACTIVE, message )
        domainDiscoveryReplyFound = createDomainDiscoveryReply(asServices([serviceName]))
        domainDiscoveryReplyNotFound = createDomainDiscoveryReply(asServices([]))
    }

    List<se.laz.casual.network.messages.Service> asServices(List<String> serviceNames)
    {
        List<se.laz.casual.network.messages.Service> l = new ArrayList<>()
        for(String s : serviceNames)
        {
            l.add(se.laz.casual.network.messages.Service.newBuilder()
            .setName(s)
            .setCategory('')
            .setTransactionType(MessageCreator.toTransactionType(TransactionType.AUTOMATIC))
            .build())
        }
        return l
    }

    CasualReply createDomainDiscoveryReply(List<se.laz.casual.network.messages.Service> services)
    {
        CasualDomainDiscoveryReply reply = CasualDomainDiscoveryReply.newBuilder()
                .addAllServices(services)
                .setDomainId(MessageCreator.toUUID4(domainId))
                .setDomainName(domainName)
                .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.DOMAIN_DISCOVERY_REPLY)
                .setCorrelationId(MessageCreator.toUUID4(executionId))
                .setDomainDiscovery(reply)
                .build()
    }

    CasualReply createServiceCallReplyMessage(ErrorState errorState, TransactionState transactionState, JsonBuffer message )
    {
        CasualServiceCallReply reply = CasualServiceCallReply.newBuilder()
                .setExecution(MessageCreator.toUUID4(executionId))
                .setResult(MessageCreator.toErrorState(errorState))
                .setTransactionState(MessageCreator.toTransactionState(transactionState))
                .setXid(MessageCreator.toXID(XID.NULL_XID))
                .setBufferTypeName('json')
                .setPayload(ByteString.copyFrom(message.getBytes().get(0)))
                .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.SERVICE_CALL_REPLY)
                .setCorrelationId(MessageCreator.toUUID4(executionId))
                .setServiceCall(reply)
                .build()
    }

    CasualReply createServiceCallReplyMessageError(ErrorState errorState, TransactionState transactionState)
    {
        CasualServiceCallReply reply = CasualServiceCallReply.newBuilder()
                .setExecution(MessageCreator.toUUID4(executionId))
                .setResult(MessageCreator.toErrorState(errorState))
                .setTransactionState(MessageCreator.toTransactionState(transactionState))
                .setXid(MessageCreator.toXID(XID.NULL_XID))
                .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.SERVICE_CALL_REPLY)
                .setCorrelationId(MessageCreator.toUUID4(executionId))
                .setServiceCall(reply)
                .build()
    }

    def "Tpcall service is available performs service call and returns result of service call."()
    {
        when:
        ServiceReturn<CasualBuffer> result = instance.tpcall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))

        then:
        result != null
        result.getServiceReturnState() == ServiceReturnState.TPSUCCESS

        1 * networkConnection.request( _ ) >> {

            CasualRequest input ->
                actualServiceRequest = input
                return new CompletableFuture<>(serviceReply)
        }
        expect actualServiceRequest.getServiceCall(), matching( expectedServiceRequest )
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
            CasualRequest input ->
                actualServiceRequest = input
                return new CompletableFuture<>(serviceReply)
        }

        expect actualServiceRequest.getServiceCall(), matching( expectedServiceRequest )
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
            CasualRequest input ->
                actualServiceRequest = input
                return new CompletableFuture<>(serviceReply)
        }

        expect actualServiceRequest.getServiceCall(), matching( expectedServiceRequest )
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
            CasualRequest input ->
                actualServiceRequest = input
                return new CompletableFuture<>(serviceReply)
        }

        expect actualServiceRequest.getServiceCall(), matching( expectedServiceRequest )
    }

    def 'tpacall fails'()
    {
        when:
        CompletableFuture<ServiceReturn<CasualBuffer>> result = instance.tpacall( serviceName, message, Flag.of( AtmiFlags.NOFLAG))
        then:
        noExceptionThrown()
        result.isCompletedExceptionally()

        1 * networkConnection.request( _ ) >> {
            CasualRequest input ->
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
            CasualRequest input ->
                actualDomainDiscoveryRequest = input
                return new CompletableFuture<>(domainDiscoveryReplyFound)
        }
        expect actualDomainDiscoveryRequest.getDomainDiscovery(), matching(expectedDomainDiscoveryRequest)
    }

    def 'serviceExists - not found'()
    {
        when:
        def r = instance.serviceExists(serviceName)
        then:
        noExceptionThrown()
        r == false
        1 * networkConnection.request(_) >> {
            CasualRequest input ->
                actualDomainDiscoveryRequest = input
                return new CompletableFuture<>(domainDiscoveryReplyNotFound)
        }
        expect actualDomainDiscoveryRequest.getDomainDiscovery(), matching(expectedDomainDiscoveryRequest)
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
    }


    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualServiceCaller" )
    }
}
