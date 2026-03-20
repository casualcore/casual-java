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

   def 'isProtocolVersionGreaterOrEqualToOneTwo'()
   {
      expect:
      ProtocolVersion.isGreaterOrEqualToOneTwo(protocolVersion) == expectedOutcome
      where:
      protocolVersion               | expectedOutcome
      ProtocolVersion.VERSION_1_0   | false
      ProtocolVersion.VERSION_1_1   | false
      ProtocolVersion.VERSION_1_2   | true
      ProtocolVersion.VERSION_1_3   | true
      ProtocolVersion.VERSION_1_4   | true
   }

   def 'isProtocolVersionGreaterOrEqualToOneThree'()
   {
      expect:
      ProtocolVersion.isGreaterOrEqualToOneThree(protocolVersion) == expectedOutcome
      where:
      protocolVersion               | expectedOutcome
      ProtocolVersion.VERSION_1_0   | false
      ProtocolVersion.VERSION_1_1   | false
      ProtocolVersion.VERSION_1_2   | false
      ProtocolVersion.VERSION_1_3   | true
      ProtocolVersion.VERSION_1_4   | true
   }

   def 'isProtocolVersionGreaterOrEqualToOneFour'()
   {
      expect:
      ProtocolVersion.isGreaterOrEqualToOneFour(protocolVersion) == expectedOutcome
      where:
      protocolVersion               | expectedOutcome
      ProtocolVersion.VERSION_1_0   | false
      ProtocolVersion.VERSION_1_1   | false
      ProtocolVersion.VERSION_1_2   | false
      ProtocolVersion.VERSION_1_3   | false
      ProtocolVersion.VERSION_1_4   | true
   }

   def 'supports domain topology change'()
   {
      expect:
      ProtocolVersion.supportsDomainTopologyChange(protocolVersion) == expectedOutcome
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
      ProtocolVersion.supportsDomainDisconnect(protocolVersion) == expectedOutcome
      where:
      protocolVersion               | expectedOutcome
      ProtocolVersion.VERSION_1_0   | false
      ProtocolVersion.VERSION_1_1   | true
      ProtocolVersion.VERSION_1_2   | true
      ProtocolVersion.VERSION_1_3   | true
      ProtocolVersion.VERSION_1_4   | true
   }

}
