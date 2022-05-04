/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.embedded.EmbeddedChannel
import se.laz.casual.api.buffer.type.JsonBuffer
import se.laz.casual.api.buffer.type.ServiceBuffer
import se.laz.casual.api.conversation.Duplex
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.connection.CasualConnectionException
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.conversation.Request
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import se.laz.casual.network.protocol.messages.service.CasualServiceCallReplyMessage
import spock.lang.Shared
import spock.lang.Specification

import javax.enterprise.concurrent.ManagedExecutorService
import java.util.concurrent.*

class NettyNetworkConnectionTest extends Specification implements NetworkListener
{
    @Shared UUID corrid = UUID.randomUUID()
    @Shared NettyNetworkConnection instance
    @Shared NettyConnectionInformation ci
    @Shared Correlator correlator
    @Shared ConversationMessageStorage conversationMessageStorage
    @Shared EmbeddedChannel channel
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
        instance = new NettyNetworkConnection(ci, correlator, channel, conversationMessageStorage, testExecutorService)
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
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> m = createRequestMessage()
        when:
        CompletableFuture<CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage>> f = instance.request(m)
        channel.writeOneInbound(m)
        CasualNWMessageImpl<CasualServiceCallReplyMessage> reply = f.get()
        then:
        noExceptionThrown()
        channel.outboundMessages().size() == 1
        reply == m
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
        instance = new NettyNetworkConnection(ci, correlator, channel, conversationMessageStorage, Mock(ManagedExecutorService))
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
        def newInstance = new NettyNetworkConnection(ci, correlator, channel, conversationMessageStorage, Mock(ManagedExecutorService))
        def future = channel.closeFuture().addListener({ f -> se.laz.casual.network.outbound.NettyNetworkConnection.handleClose(newInstance, this) })
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
        def localInstance = new NettyNetworkConnection(ci, correlator, channel, Mock(ConversationMessageStorage), Mock(ManagedExecutorService))
        CasualNWMessageImpl<CasualDomainDiscoveryRequestMessage> requestMessage = createRequestMessage()
        when:
        localInstance.request(requestMessage)
        then:
        networkError == true
    }

    def createRequestMessage()
    {
        CasualDomainDiscoveryRequestMessage message = CasualDomainDiscoveryRequestMessage.createBuilder()
                .setExecution(UUID.randomUUID())
                .setDomainId(UUID.randomUUID())
                .setDomainName( "test-domain" )
                .setServiceNames(Arrays.asList("echo"))
                .build()
        return CasualNWMessageImpl.of(corrid, message)
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
    void disconnected()
    {
        casualDisconnected = true;
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
   }
}
