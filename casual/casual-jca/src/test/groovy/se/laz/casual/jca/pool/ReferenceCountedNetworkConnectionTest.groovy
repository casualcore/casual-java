package se.laz.casual.jca.pool

import se.laz.casual.network.outbound.NettyNetworkConnection
import spock.lang.Specification

class ReferenceCountedNetworkConnectionTest extends Specification
{
   def 'test only 1 reference count ( initial )'()
   {
      given:
      def networkConnection = Mock(NettyNetworkConnection) {
         1 * close()
      }
      def closeListener = Mock(ReferenceCountedNetworkCloseListener){
         1 * closed(_)
      }
      // 1st reference count on creation
      ReferenceCountedNetworkConnection instance = ReferenceCountedNetworkConnection.of(networkConnection, closeListener)
      when:
      instance.close()
      then:
      noExceptionThrown()
   }

   def 'test reference counting and effects when the last reference is closed - more than initial reference'()
   {
      given:
      def subsequentReferences = 4
      def expectedNumberOfReferences = 5
      def networkConnection = Mock(NettyNetworkConnection) {
         1 * close()
      }
      def closeListener = Mock(ReferenceCountedNetworkCloseListener){
         1 * closed(_)
      }
      // 1st reference count on creation
      ReferenceCountedNetworkConnection instance = ReferenceCountedNetworkConnection.of(networkConnection, closeListener)
      for(int i = 0; i < subsequentReferences; ++i)
      {
         instance.increment()
      }
      when:
      for(int i = 0; i < expectedNumberOfReferences; ++i)
      {
         instance.close()
      }
      then:
      noExceptionThrown()
   }
}
