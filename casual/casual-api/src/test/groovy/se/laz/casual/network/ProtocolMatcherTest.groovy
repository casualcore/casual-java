/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network


import spock.lang.Specification

class ProtocolMatcherTest extends Specification
{
   def 'null'()
   {
      when:
      ProtocolMatcher.match(null)
      then:
      thrown(NullPointerException)
   }

   def 'correctness'()
   {
      given:
      def matchLowest = [ProtocolVersion.VERSION_1_0.version]
      def matchHighest = [ProtocolVersion.VERSION_1_0.version, ProtocolVersion.VERSION_1_1.version, ProtocolVersion.VERSION_1_2.version]
      def unordered = [ProtocolVersion.VERSION_1_2.version, ProtocolVersion.VERSION_1_0.version, ProtocolVersion.VERSION_1_1.version]
      def mismatch = [999L]
      when:
      def lowest = ProtocolMatcher.match(matchLowest)
      def highest = ProtocolMatcher.match(matchHighest)
      def unorderedMatch = ProtocolMatcher.match(unordered)
      then:
      ProtocolVersion.VERSION_1_0 == ProtocolVersion.unmarshall(lowest)
      ProtocolVersion.VERSION_1_2 == ProtocolVersion.unmarshall(highest)
      ProtocolVersion.VERSION_1_2 == ProtocolVersion.unmarshall(unorderedMatch)
      when:
      ProtocolMatcher.match(mismatch)
      then:
      thrown(ProtocolVersionException)
   }

}
