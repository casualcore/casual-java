/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Specification

class SpanIdTest extends Specification
{
   def 'sanity'()
   {
      when:
      def spanId = SpanId.of(value)
      def spanIdTwo = SpanId.of(value)
      then:
      spanId.asUnsignedLong() == value
      spanId.equals(spanIdTwo)
      where:
      value << (1L .. 100L)
   }

   def 'auto construction'()
   {
      when:
      SpanId.of()
      then:
      noExceptionThrown()
   }

}
