package se.laz.casual.event.server

import spock.lang.Specification
import io.netty.channel.Channel;

class EventServerTest extends Specification
{
   def 'testing test'()
   {
      given:
      Channel channel = Mock(Channel){
         isActive() >> true
      }
      ServerInitialization serverInitialization = Mock(ServerInitialization){
         1* it.init(*_) >> {
            channel
         }
      }
      EventServerConnectionInformation connectionInformation = EventServerConnectionInformation.createBuilder()
              .withServerInitialization (serverInitialization)
              .withPort(123456)
              .build()
      when:
      EventServer server = EventServer.of(connectionInformation)
      then:
      server.isActive()
   }
}
