/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.extension


import se.laz.casual.jca.inbound.handler.test.TestCasualServiceHandlerExtensionLowestPriority
import se.laz.casual.jca.inbound.handler.test.TestHandler
import spock.lang.Specification

class ServiceHandlerExtensionFactoryTest extends Specification
{
   def "GetHandler service in the right order when multiple can handle, also get default when no extension registered for name"()
   {
      when:
      ServiceHandlerExtension extension = ServiceHandlerExtensionFactory.getExtension( extensionName )

      then:
      extension.getClass() == extensionType

      where:
      extensionName                                  | extensionType
      TestHandler.class.getName()                    | TestCasualServiceHandlerExtensionLowestPriority.class
      'unknown'                                      | DefaultServiceHandlerExtension.class
   }
}
