package se.laz.casual.network.outbound

import io.netty.channel.epoll.EpollSocketChannel
import se.laz.casual.config.Outbound

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable
import io.netty.channel.socket.nio.NioSocketChannel
import se.laz.casual.network.ProtocolVersion
import spock.lang.Specification

class NettyConnectionInformationCreatorTest extends Specification
{
   def 'default, does not use epoll'()
   {
      given:
      InetSocketAddress address = new InetSocketAddress('foo.bar', 1234)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      when:
      NettyConnectionInformation ci = NettyConnectionInformationCreator.create(address, protocolVersion)
      then:
      ci.getChannelClass() == NioSocketChannel.class
   }

   def 'use epoll'()
   {
      given:
      InetSocketAddress address = new InetSocketAddress('foo.bar', 1234)
      ProtocolVersion protocolVersion = ProtocolVersion.VERSION_1_0
      when:
      NettyConnectionInformation ci
      withEnvironmentVariable( Outbound.USE_EPOLL_ENV_VAR_NAME, "true" )
              .execute( {
                 ci = NettyConnectionInformationCreator.create(address, protocolVersion)
                 } )

      then:
      ci.getChannelClass() == EpollSocketChannel.class
   }
}
