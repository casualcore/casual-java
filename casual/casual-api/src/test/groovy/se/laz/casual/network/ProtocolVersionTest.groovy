/*
 * Copyright (c) 2022 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network


import spock.lang.Specification

class ProtocolVersionTest extends Specification
{
   def "unmarshalling"()
   {
      expect:
      for( ProtocolVersion p : ProtocolVersion.values(  ) )
      {
         assert ProtocolVersion.unmarshall( p.getVersion(  ) ) == p
         assert ProtocolVersion.unmarshall( p.getVersionAsString(  ) ) == p
      }
   }

   def "unsupported version long"()
   {
      when:
      ProtocolVersion.unmarshall( value )

      then:
      thrown ProtocolVersionException

      where:
      value << [
              0L,
              -1L
      ]
   }

   def "Unsupported version string"()
   {
      when:
      ProtocolVersion.unmarshall( value )

      then:
      thrown ProtocolVersionException

      where:
      value << [
              null,
              "",
              "12"
      ]
   }

   def "#protocolVersion isGreaterThanOrEqualTo #comparingVersion"()
   {
      expect:
      protocolVersion.isGreaterThanOrEqualTo( comparingVersion ) == expectedOutcome
      where:
      protocolVersion               | comparingVersion | expectedOutcome
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_4 | false

      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_4 | false

      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_4 | false

      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_4 | false

      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_4 | true
   }

   def "#protocolVersion isGreaterThan #comparingVersion"()
   {
      expect:
      protocolVersion.isGreaterThan( comparingVersion ) == expectedOutcome
      where:
      protocolVersion               | comparingVersion | expectedOutcome
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_4 | false

      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_4 | false

      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_4 | false

      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_4 | false

      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_4 | false
   }

   def "#protocolVersion isLessThanOrEqualTo #comparingVersion"()
   {
      expect:
      protocolVersion.isLessThanOrEqualTo( comparingVersion ) == expectedOutcome
      where:
      protocolVersion               | comparingVersion | expectedOutcome
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_0 | true
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_4 | true

      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_4 | true

      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_4 | true

      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_4 | true

      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_4 | true
   }

   def "#protocolVersion isLessThan #comparingVersion"()
   {
      expect:
      protocolVersion.isLessThan( comparingVersion ) == expectedOutcome
      where:
      protocolVersion               | comparingVersion | expectedOutcome
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_1 | true
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_0   | ProtocolVersion.VERSION_1_4 | true

      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_2 | true
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_1   | ProtocolVersion.VERSION_1_4 | true

      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_3 | true
      ProtocolVersion.VERSION_1_2   | ProtocolVersion.VERSION_1_4 | true

      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_3   | ProtocolVersion.VERSION_1_4 | true

      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_0 | false
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_1 | false
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_2 | false
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_3 | false
      ProtocolVersion.VERSION_1_4   | ProtocolVersion.VERSION_1_4 | false
   }

   def 'supports domain topology change'()
   {
      expect:
      protocolVersion.supportsDomainTopologyChange() == expectedOutcome
      where:
      protocolVersion               | expectedOutcome
      ProtocolVersion.VERSION_1_0   | false
      ProtocolVersion.VERSION_1_1   | false
      ProtocolVersion.VERSION_1_2   | true
      ProtocolVersion.VERSION_1_3   | true
      ProtocolVersion.VERSION_1_4   | true
   }

   def 'supports domain disconnect'()
   {
      expect:
      protocolVersion.supportsDomainDisconnect() == expectedOutcome
      where:
      protocolVersion               | expectedOutcome
      ProtocolVersion.VERSION_1_0   | false
      ProtocolVersion.VERSION_1_1   | true
      ProtocolVersion.VERSION_1_2   | true
      ProtocolVersion.VERSION_1_3   | true
      ProtocolVersion.VERSION_1_4   | true
   }

}
