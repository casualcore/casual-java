/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Specification

import java.nio.ByteBuffer

class SpanIdTest extends Specification
{
   def 'sanity'()
   {
      when:
      def spanId = SpanId.of(value)
      def spanIdTwo = SpanId.of(value)
      then:
      spanId.asHex().length() == 16
      spanId == spanIdTwo
      where:
      value << (1L .. 100L).collect { long v ->
         ByteBuffer.allocate(8).putLong(v).array()
      }
   }

   def 'auto construction'()
   {
      when:
      def spanId = SpanId.of()
      then:
      spanId.asHex().length() == 16
   }

   def "generated SpanIds are unique"() {
      given:
      def hexIds = (1L..100L).collect { SpanId.of().asHex() }
      expect:
      hexIds.toSet().size() == hexIds.size()
   }

}
