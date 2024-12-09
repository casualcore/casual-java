/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.impl.gson

import se.laz.casual.api.external.json.JsonProviderFactory
import spock.lang.Specification

class GsonProviderTest extends Specification
{
   def 'roundtrip'()
   {
      given:
      def point = new Point(10,20)
      when:
      def json = JsonProviderFactory.getJsonProvider().toJson(point)
      then:
      noExceptionThrown()
      when:
      def resurrected = JsonProviderFactory.getJsonProvider().fromJson(json, Point.class)
      then:
      resurrected == point
   }
}
