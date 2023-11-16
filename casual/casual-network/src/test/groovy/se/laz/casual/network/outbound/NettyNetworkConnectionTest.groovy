/*
 * Copyright (c) 2017 - 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.embedded.EmbeddedChannel
import jakarta.enterprise.concurrent.ContextService
import jakarta.enterprise.concurrent.ManagedExecutorService
import se.laz.casual.api.buffer.type.CStringBuffer
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.conversation.Duplex
import se.laz.casual.api.flags.AtmiFlags
import se.laz.casual.api.flags.Flag
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.xa.XID
import se.laz.casual.jca.DomainId
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.conversation.Request
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage

import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.xa.Xid
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Supplier

class NettyNetworkConnectionTest extends Specification implements NetworkListener
{
    @Shared UUID corrid = UUID.randomUUID()
    @Shared byte[] gtrid = 'asdf' as byte[]
    @Shared byte[] bqual = 'qwerty' as byte[]
    @Shared Xid xid = XID.of(gtrid, bqual, 128)
    @Shared NettyNetworkConnection instance
    @Shared NettyConnectionInformation ci
    @Shared Correlator correlator
    @Shared ConversationMessageStorage conversationMessageStorage
    @Shared EmbeddedChannel channel
    @Shared DomainId casualDomainId = DomainId.of(UUID.randomUUID())
    private boolean casualDisconnected = false;

    def setup()
    {
        TestExecutorService testExecutorService = new TestExecutorService()
        conversationMessageStorage = ConversationMessageStorageImpl.of()
        correlator = CorrelatorImpl.of()
        ci = NettyConnectionInformation.createBuilder()
                                                            .withAddress(new InetSocketAddress(3712))
                                                            .withProtocolVersion(ProtocolVersion.VERSION_1_0)
                                                            .withDomainId(UUID.randomUUID())
                                                            .withDomainName('testDomain')
                                                            .withCorrelator(correlator)
                                                            .build()
        def conversationMessageHandler = ConversationMessageHandler.of(conversationMessageStorage)
        channel = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), conversationMessageHandler, ExceptionHandler.of(correlator, Mock(OnNetworkError)))
        instance = new NettyNetworkConnection(ci, correlator, channel, conversationMessageStorage, {testExecutorService}, Mock(ErrorInformer))
        instance.setDomainId(casualDomainId)
    }

    def 'Of with a null connection info throws NullPointerException.'()
    {
        when:
        NettyNetworkConnection.of(connectionInformation, networkListener)
        then:
        thrown NullPointerException
        where:
        connectionInformation            | networkListener
        null                             | Mock(NetworkListener)
        ci                               | null
    }

    def 'ping ponging a domain discovery request message'()
    {
        setup:
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> requestMessage = createDomainDiscoveryRequestMessage()
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> replyMessage = createDomainDiscoveryReplyMessage()
        when:
        CompletableFuture<CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage>> f = instance.request(requestMessage)
        channel.writeOneInbound(replyMessage)
        CasualNWMessageImpl<CasualDomainDiscoveryReplyMessage> reply = f.get()
        then:
        noExceptionThrown()
        channel.outboundMessages().size() == 1
        reply == replyMessage
    }

    def 'tpacall TPNOREPLY'()
    {
       setup:
       CasualNWMessageImpl<CasualServiceCallRequestMessage> requestMessage = createServiceCallRequestMessage(true, false)
       when:
       instance.requestNoReply(requestMessage)
       then:
       0 * correlator.complete(_)
       0 * correlator.completeExceptionally(_)
       0 * correlator.completeAllExceptionally(_)
       noExceptionThrown()
       channel.outboundMessages().size() == 1
    }

    def 'send conversation request message, no message is stored'()
    {
       given:
       conversationMessageStorage.clearAllConversations()
       CasualNWMessage<Request> requestMessage = CasualNWMessageImpl.of(UUID.randomUUID(), Request.createBuilder()
               .setExecution(UUID.randomUUID())
               .setDuplex(Duplex.RECEIVE)
               .setServiceBuffer(ServiceBuffer.of(JsonBuffer.of('{"hello":"world"}')))
               .build())
       when:
       instance.send(requestMessage)
       then:
       conversationMessageStorage.numberOfConversations() == 0
    }

   def 'send fail'()
   {
      given:
      CasualNWMessage<Request> requestMessage = Mock()
      when:
      instance.send(requestMessage)
      then:
      def e = thrown( CompletionException )
      e.cause.class == CasualConnectionException
   }

   def 'recv one conversation request message'()
   {
      given:
      def corrId = UUID.randomUUID()
      CasualNWMessage<Request> requestMessage = CasualNWMessageImpl.of(corrId, Request.createBuilder()
              .setExecution(UUID.randomUUID())
              .setDuplex(Duplex.SEND)
              .setResultCode(0)
              .setServiceBuffer(ServiceBuffer.empty())
              .build())
      when:
      channel.writeOneInbound(requestMessage)
      then:
      conversationMessageStorage.size(corrId) == 1
      when:
      CompletableFuture<CasualNWMessage<Request>> future = instance.receive(corrId)
      def inboundMsg = future.join()
      then:
      inboundMsg == requestMessage
      conversationMessageStorage.size(corrId) == 0
   }

    def 'close'()
    {
        setup:
        def channel = Mock(Channel)
        1 * channel.close()
        instance = new NettyNetworkConnection(ci, correlator, channel, conversationMessageStorage, {Mock(ManagedExecutorService)}, Mock(ErrorInformer))
        instance.setDomainId(casualDomainId)
        when:
        instance.close()
        then:
        noExceptionThrown()
        casualDisconnected == false
    }

    def 'casual disconnected'()
    {
        given:
        def channel = new EmbeddedChannel(CasualNWMessageDecoder.of(), CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator, Mock(OnNetworkError)))
        instance = new NettyNetworkConnection(ci, correlator, channel, conversationMessageStorage, {Mock(ManagedExecutorService)}, Mock(ErrorInformer))
        instance.setDomainId(casualDomainId)
        ErrorInformer errorInformer = ErrorInformer.of(new CasualConnectionException("connection gone"))
        errorInformer.addListener(this)
        def future = channel.closeFuture().addListener({ f -> se.laz.casual.network.outbound.NettyNetworkConnection.handleClose(instance, errorInformer) })
        when:
        future.channel().disconnect()
        then:
        casualDisconnected == true
    }


    def 'exception when reading'()
    {
        given:
        def messageDecoder = Mock(CasualNWMessageDecoder){
            channelRead(_, _) >> { ChannelHandlerContext ctx, Object msg ->
                ctx.fireExceptionCaught(new IOException("boom"))
            }
        }
        def onNetworkError = Mock(OnNetworkError)
        def channel = new EmbeddedChannel(messageDecoder, CasualNWMessageEncoder.of(), CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator, onNetworkError))
        def networkError = false
        onNetworkError.notifyListenerIfNotConnected(channel) >> {
            networkError = true
        }
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> replyMessage = createReplyMessage()
        when:
        channel.writeOneInbound(replyMessage)
        channel.flushInbound()
        then:
        networkError == true
    }

    def 'exception when writing'()
    {
        given:
        def messageEncoder = Mock(CasualNWMessageEncoder){
            write(_, _, _) >> { ChannelHandlerContext ctx, Object msg, ChannelPromise promise ->
                ctx.fireExceptionCaught(new IOException("boom"))
            }
        }
        def onNetworkError = Mock(OnNetworkError)
        def channel = new EmbeddedChannel(CasualNWMessageDecoder.of(), messageEncoder, CasualMessageHandler.of(correlator), ExceptionHandler.of(correlator, onNetworkError))
        def networkError = false
        onNetworkError.notifyListenerIfNotConnected(channel) >> {
            networkError = true
        }
        def localInstance = new NettyNetworkConnection(ci, correlator, channel, Mock(ConversationMessageStorage), {Mock(ManagedExecutorService)}, Mock(ErrorInformer))
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> requestMessage = createDomainDiscoveryRequestMessage()
        when:
        localInstance.request(requestMessage)
        then:
        networkError == true
    }

    def createDomainDiscoveryRequestMessage()
    {
        CasualDomainDiscoveryRequestMessage message = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setDomainName( "test-domain" )
                .setServiceNames(Arrays.asList("echo"))
                .build()
        return CasualNWMessageImpl.of(corrid, message)
    }

    def createDomainDiscoveryReplyMessage()
    {
       CasualDomainDiscoveryReplyMessage message = CasualDomainDiscoveryReplyMessage.of(UUID.randomUUID(), UUID.randomUUID(), 'test-domain')
       return CasualNWMessageImpl.of(corrid, message)
   }

    def createServiceCallRequestMessage(boolean tpnoreply, boolean transactional)
    {
       ServiceBuffer buffer = ServiceBuffer.of(CStringBuffer.of('asdf'))
       CasualServiceCallRequestMessage.Builder builder = CasualServiceCallRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setServiceName('test-service')
                .setServiceBuffer(buffer)
       if(tpnoreply)
       {
          builder.setXatmiFlags(Flag.of(AtmiFlags.TPNOREPLY))
       }
       if(transactional)
       {
          builder.setXid(xid)
       }
       else
       {
          builder.setXid(XID.NULL_XID)
       }
       return CasualNWMessageImpl.of(corrid, builder.build())
   }

    def createReplyMessage()
    {
        CasualDomainDiscoveryReplyMessage message = CasualDomainDiscoveryReplyMessage.of(UUID.randomUUID(), UUID.randomUUID(), 'test-domain')
        return CasualNWMessageImpl.of(corrid, message)
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains 'NettyNetworkConnection'
    }

    @Override
    void disconnected(Exception reason)
    {
        casualDisconnected = true
        // Note: this is what the appserver would do
        instance.close()
    }

   class TestExecutorService implements ManagedExecutorService
   {
      @Override
      void shutdown()
      {}

      @Override
      List<Runnable> shutdownNow()
      {
         return null
      }

      @Override
      boolean isShutdown()
      {
         return false
      }

      @Override
      boolean isTerminated()
      {
         return false
      }

      @Override
      boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException
      {
         return false
      }

      @Override
      def <T> java.util.concurrent.Future<T> submit(Callable<T> callable)
      {
         return null
      }

      @Override
      def <T> java.util.concurrent.Future<T> submit(Runnable runnable, T t)
      {
         return null
      }

      @Override
      java.util.concurrent.Future<?> submit(Runnable runnable)
      {
         return null
      }

      @Override
      def <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException
      {
         return null
      }

      @Override
      def <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException
      {
         return null
      }

      @Override
      def <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException
      {
         return null
      }

      @Override
      def <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException
      {
         return null
      }

      @Override
      void execute(Runnable runnable)
      {
         CompletableFuture.runAsync(runnable)
      }

      @Override
      def <U> CompletableFuture<U> completedFuture(U value) {
         return null
      }

      @Override
      def <U> CompletionStage<U> completedStage(U value) {
         return null
      }

      @Override
      def <T> CompletableFuture<T> copy(CompletableFuture<T> stage) {
         return null
      }

      @Override
      def <T> CompletionStage<T> copy(CompletionStage<T> stage) {
         return null
      }

      @Override
      def <U> CompletableFuture<U> failedFuture(Throwable ex) {
         return null
      }

      @Override
      def <U> CompletionStage<U> failedStage(Throwable ex) {
         return null
      }

      @Override
      ContextService getContextService() {
         return null
      }

      @Override
      def <U> CompletableFuture<U> newIncompleteFuture() {
         return null
      }

      @Override
      CompletableFuture<Void> runAsync(Runnable runnable) {
         return null
      }

      @Override
      def <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
         return null
      }
   }
}
