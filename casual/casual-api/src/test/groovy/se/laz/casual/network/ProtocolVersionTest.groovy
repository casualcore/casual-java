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

   def 'isProtocolVersionGreaterOrEqualToOneTwo true'()
   {
      expect:
      ProtocolVersion.isProtocolVersionGreaterOrEqualToOneTwo(protocolVersion) == true
      where:
      protocolVersion << [ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
   }

   def 'isProtocolVersionGreaterOrEqualToOneTwo false'()
   {
      expect:
      ProtocolVersion.isProtocolVersionGreaterOrEqualToOneTwo(protocolVersion) == false
      where:
      protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1]
   }

   def 'isProtocolVersionGreaterOrEqualToOneThree true'()
   {
      expect:
      ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion) == true
      where:
      protocolVersion << [ProtocolVersion.VERSION_1_3, ProtocolVersion.VERSION_1_4]
   }

   def 'isProtocolVersionGreaterOrEqualToOneThree false'()
   {
      expect:
      ProtocolVersion.isProtocolVersionGreaterOrEqualToOneThree(protocolVersion) == false
      where:
      protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2]
   }

   def 'isProtocolVersionGreaterOrEqualToOneFour true'()
   {
      expect:
      ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion) == true
      where:
      protocolVersion << [ProtocolVersion.VERSION_1_4]
   }

   def 'isProtocolVersionGreaterOrEqualToOneFour false'()
   {
      expect:
      ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion) == false
      where:
      protocolVersion << [ProtocolVersion.VERSION_1_0, ProtocolVersion.VERSION_1_1, ProtocolVersion.VERSION_1_2, ProtocolVersion.VERSION_1_3]
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
