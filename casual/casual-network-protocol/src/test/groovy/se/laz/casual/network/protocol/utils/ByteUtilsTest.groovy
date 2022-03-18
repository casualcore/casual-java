package se.laz.casual.network.protocol.utils

import spock.lang.Specification

class ByteUtilsTest extends Specification
{
   def 'no data'()
   {
      when:
      def size = ByteUtils.sumNumberOfBytes(new ArrayList<byte[]>())
      then:
      size == 0
   }
}
