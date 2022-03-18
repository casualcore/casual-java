/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type


import spock.lang.Specification

class OctetBufferTest extends Specification
{
   def 'null construction'()
   {
      when:
      OctetBuffer.of(null)
      then:
      thrown(NullPointerException)
   }

   def 'correct data returned'()
   {
      given:
      byte[] data = 'asdf' as byte[]
      def buffer = OctetBuffer.of(data)
      when:
      List<byte[]> returned = buffer.getBytes()
      then:
      null != returned
      returned.size() == 1
      returned[0] == data
   }

}
