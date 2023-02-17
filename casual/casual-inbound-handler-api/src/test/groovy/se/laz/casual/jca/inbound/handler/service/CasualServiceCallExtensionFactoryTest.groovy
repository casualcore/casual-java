/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service

import se.laz.casual.jca.inbound.handler.service.casual.DefaultCasualServiceHandler
import se.laz.casual.jca.inbound.handler.test.TestCasualServiceCallExtensionLowestPriority
import spock.lang.Specification

class CasualServiceCallExtensionFactoryTest extends Specification
{
   def "GetHandler service in the right order when multiple can handle, also get default when no extension registered for name"()
   {
      when:
      CasualServiceCallExtension extension = CasualServiceCallExtensionFactory.getExtension( extensionName )

      then:
      extension.getClass() == extensionType

      where:
      extensionName                               | extensionType
      DefaultCasualServiceHandler.class.getName() | TestCasualServiceCallExtensionLowestPriority.class
      'unknown'                                   | DefaultCasualServiceCallExtension.class
   }
}
