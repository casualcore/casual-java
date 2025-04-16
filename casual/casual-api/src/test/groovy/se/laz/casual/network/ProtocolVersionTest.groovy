/*
 * Copyright (c) 2022 - 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network

import se.laz.casual.network.connection.CasualConnectionException
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
      thrown CasualConnectionException

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
      thrown CasualConnectionException

      where:
      value << [
              null,
              "",
              "12"
      ]
   }

}
