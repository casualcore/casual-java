/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.reverse.outbound

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import se.laz.casual.api.network.protocol.messages.CasualNWMessage
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import se.laz.casual.jca.DomainId
import se.laz.casual.network.CasualNWMessageDecoder
import se.laz.casual.network.CasualNWMessageEncoder
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.inbound.ProtocolVersionValueHolder
import se.laz.casual.network.outbound.NettyNetworkConnection
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Loopback test of the full reverse outbound flow:
 * bind - fake casual client ( the reverse inbound side ) connects and waits - we issue the domain connect
 * request - the client replies - established connection delivered to the consumer - request/reply as outbound.
 */
class ReverseOutboundServerImplTest extends Specification
{
   static final long TIMEOUT_SECONDS = 5

   UUID serverDomainId = UUID.randomUUID()
   UUID clientDomainId = UUID.randomUUID()
   NioEventLoopGroup clientGroup
   ReverseOutboundServer server
   Channel clientChannel

   def setupSpec()
   {
      // no app server in tests
      ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_UNMANAGED, true )
   }

   def cleanupSpec()
   {
      ConfigurationService.reload()
   }

   def cleanup()
   {
      server?.deactivate()
      clientGroup?.shutdownGracefully()?.syncUninterruptibly()
   }

   def 'handshake and request/reply over an established reverse outbound connection'()
   {
      given: 'a reverse outbound server on an ephemeral port'
      CompletableFuture<NettyNetworkConnection> deliveredConnection = new CompletableFuture<>()
      ReverseOutboundConnectionInformation ci = ReverseOutboundConnectionInformation.createBuilder()
              .withName( 'myReverseOutbound' )
              .withPort( 0 )
              .withDomainId( serverDomainId )
              .withDomainName( 'reverseDomain' )
              .withUseEpoll( false )
              .withConnectionConsumer( { connection -> deliveredConnection.complete( connection ) } )
              .build()
      server = ReverseOutboundServerImpl.of( ci )

      and: 'a fake casual client, the reverse inbound side, that waits for the domain connect request'
      CompletableFuture<CasualNWMessage<CasualDomainConnectRequestMessage>> connectRequest = new CompletableFuture<>()
      clientGroup = new NioEventLoopGroup( 1 )
      Bootstrap bootstrap = new Bootstrap()
              .group( clientGroup )
              .channel( NioSocketChannel )
              .handler( new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel( SocketChannel ch )
                 {
                    ch.pipeline().addLast( CasualNWMessageDecoder.of( ProtocolVersionValueHolder.of() ), CasualNWMessageEncoder.of(), new FakeCasualClientHandler( connectRequest, clientDomainId ) )
                 }
              } )

      when: 'the client connects'
      clientChannel = bootstrap.connect( 'localhost', server.getPort() ).syncUninterruptibly().channel()
      CasualNWMessage<CasualDomainConnectRequestMessage> request = connectRequest.get( TIMEOUT_SECONDS, TimeUnit.SECONDS )

      then: 'we, the outbound side, issued the domain connect request with the configured domain'
      request.getMessage().getDomainId() == serverDomainId
      request.getMessage().getDomainName() == 'reverseDomain'
      request.getMessage().getProtocols().contains( ProtocolVersion.VERSION_1_5.getVersion() )

      when: 'the established connection is delivered to the consumer'
      NettyNetworkConnection networkConnection = deliveredConnection.get( TIMEOUT_SECONDS, TimeUnit.SECONDS )

      then: 'it is active, knows the remote ( EIS ) domain and the negotiated protocol version'
      networkConnection.isActive()
      networkConnection.getDomainId() == DomainId.of( clientDomainId )
      networkConnection.getProtocolVersion() == ProtocolVersion.VERSION_1_5

      when: 'a request is issued over the connection, acting as outbound'
      UUID correlationId = UUID.randomUUID()
      CasualDomainDiscoveryRequestMessage discoveryRequest = CasualDomainDiscoveryRequestMessage.createBuilder()
              .setExecution( UUID.randomUUID() )
              .setDomainId( serverDomainId )
              .setDomainName( 'reverseDomain' )
              .setServiceNames( ['echo'] )
              .build()
      CompletableFuture<CasualNWMessage<CasualDomainDiscoveryReplyMessage>> future = networkConnection.request( CasualNWMessageImpl.of( correlationId, discoveryRequest ) )
      CasualNWMessage<CasualDomainDiscoveryReplyMessage> discoveryReply = future.get( TIMEOUT_SECONDS, TimeUnit.SECONDS )

      then: 'the reply from the client correlates'
      discoveryReply.getCorrelationId() == correlationId
      discoveryReply.getMessage().getDomainName() == 'clientDomain'

      when: 'the server is deactivated'
      server.deactivate()
      server = null

      then: 'the established connection is closed as well'
      !networkConnection.isActive()
   }

   /**
    * Acts as the casual EIS side ( reverse inbound ): waits for the domain connect request, replies and
    * after that answers any domain discovery request as an inbound would.
    */
   static class FakeCasualClientHandler extends SimpleChannelInboundHandler<CasualNWMessage<?>>
   {
      private final CompletableFuture<CasualNWMessage<CasualDomainConnectRequestMessage>> connectRequest
      private final UUID clientDomainId

      FakeCasualClientHandler( CompletableFuture<CasualNWMessage<CasualDomainConnectRequestMessage>> connectRequest, UUID clientDomainId )
      {
         this.connectRequest = connectRequest
         this.clientDomainId = clientDomainId
      }

      @Override
      protected void channelRead0( ChannelHandlerContext ctx, CasualNWMessage<?> message )
      {
         switch( message.getType() )
         {
            case CasualNWMessageType.DOMAIN_CONNECT_REQUEST:
               CasualNWMessage<CasualDomainConnectRequestMessage> request = message as CasualNWMessage<CasualDomainConnectRequestMessage>
               CasualDomainConnectReplyMessage reply = CasualDomainConnectReplyMessage.createBuilder()
                       .withExecution( request.getMessage().getExecution() )
                       .withDomainId( clientDomainId )
                       .withDomainName( 'clientDomain' )
                       .withProtocolVersion( ProtocolVersion.VERSION_1_5.getVersion() )
                       .build()
               ctx.writeAndFlush( CasualNWMessageImpl.of( request.getCorrelationId(), reply ) )
               connectRequest.complete( request )
               break
            case CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST:
               CasualDomainDiscoveryReplyMessage discoveryReply = CasualDomainDiscoveryReplyMessage.of( UUID.randomUUID(), UUID.randomUUID(), 'clientDomain', ProtocolVersion.VERSION_1_5 )
               ctx.writeAndFlush( CasualNWMessageImpl.of( message.getCorrelationId(), discoveryReply ) )
               break
            default:
               throw new IllegalStateException( 'unexpected message type: ' + message.getType() )
         }
      }
   }
}
