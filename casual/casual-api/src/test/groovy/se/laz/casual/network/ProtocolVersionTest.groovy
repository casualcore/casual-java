/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network

import spock.lang.Specification

class ProtocolVersionTest extends Specification
{
   def 'unmarshalling'()
   {
      when:
      ProtocolVersion protocolVersion = ProtocolVersion.unmarshall(version)
      then:
      protocolVersion.name() == versionString
      where:
      version | versionString
      1000L   | 'VERSION_1_0'
      1001L   | 'VERSION_1_1'
      1002L   | 'VERSION_1_2'
   }

   def 'unsupported version'()
   {

   }

}
