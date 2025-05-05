/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow

import se.laz.casual.api.xa.XID
import spock.lang.Specification

import javax.transaction.xa.Xid

class XidKeyTest extends Specification
{
   def 'equality/hashcode'()
   {
      given:
      byte[] gtridData = (0..XID.MAX_XID_DATA_SIZE / 2 - 1) as byte[]
      byte[] bqualData = (0..XID.MAX_XID_DATA_SIZE / 2 - 1) as byte[]
      def formatTypeOne = 42L
      def formatTypeTwo = 99L
      when:
      Xid xidOne = XID.of(gtridData, bqualData, formatTypeOne)
      XidKey keyOne = XidKey.of(xidOne)
      Xid xidTwo = XID.of(gtridData, bqualData, formatTypeTwo)
      XidKey keyTwo = XidKey.of(xidTwo)
      then:
      keyOne == keyOne
      keyOne.hashCode() == keyOne.hashCode()
      keyTwo == keyTwo
      keyTwo.hashCode() == keyTwo.hashCode()
      keyOne != keyTwo
      keyOne.hashCode() != keyTwo.hashCode()
   }
}
