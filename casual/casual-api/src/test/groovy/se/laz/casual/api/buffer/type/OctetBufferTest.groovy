/*
 * Copyright (c) 2021 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.buffer.type


import se.laz.casual.api.buffer.CasualHeaders
import spock.lang.Shared
import spock.lang.Specification

class OctetBufferTest extends Specification
{
   @Shared List<String> rawHeaders = ["a:foo", "b:bar", "c:baz" ]
   @Shared CasualHeaders headers

   def setupSpec()
   {
      headers = CasualHeaders.newBuilder().addAll( rawHeaders ).build(  )
   }

   def 'null construction'()
   {
      when:
      OctetBuffer.of(null as byte[])

      then:
      thrown NullPointerException

      when:
      OctetBuffer.of(null as List<byte[]>)

      then:
      thrown NullPointerException
   }

   def 'null headers'()
   {
      given:
      byte[] data = 'asdf' as byte[]

      when:
      OctetBuffer.of(data, null )

      then:
      thrown NullPointerException

      when:
      OctetBuffer.of( [data], null )

      then:
      thrown NullPointerException
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

   def "existing buffer has empty headers."()
   {
      given:
      byte[] data = 'asdf' as byte[]
      OctetBuffer buffer = OctetBuffer.of( data )

      when:
      CasualHeaders actual = buffer.getHeaders(  )

      then:
      actual == CasualHeaders.empty(  )
   }

   def "Create buffer with headers returns headers."()
   {
      given:
      byte[] data = 'asdf' as byte[]
      OctetBuffer buffer = OctetBuffer.of( data, headers )

      when:
      CasualHeaders actual = buffer.getHeaders(  )

      then:
      actual == headers
   }


}
