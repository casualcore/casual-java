/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow

import io.netty.channel.ChannelId
import se.laz.casual.api.xa.XID
import spock.lang.Specification

import javax.transaction.xa.Xid

class CasualInboundTransactionRegistryTest extends Specification
{
   CasualInboundTransactionRegistry instance

   def setup()
   {
      instance = new CasualInboundTransactionRegistry()
   }

   def 'add, remove, pending test'()
   {
      given:
      byte[] gtridData = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
      byte[] bqualData = (0..XID.MAX_XID_DATA_SIZE/2 -1) as byte[]
      def formatTypeOne = 42L
      def formatTypeTwo = 99L
      ChannelId idOne = Mock(ChannelId)
      Xid xidOne = XID.of(gtridData, bqualData, formatTypeOne)
      XidKey keyOne = XidKey.of(xidOne)
      ChannelId idTwo = Mock(ChannelId)
      Xid xidTwo = XID.of(gtridData, bqualData, formatTypeTwo)
      XidKey keyTwo = XidKey.of(xidTwo)
      when:
      instance.add(idOne, keyOne)
      instance.add(idOne, keyTwo)
      then:
      instance.hasPending()
      when:
      instance.remove(idOne, keyOne)
      then:
      instance.hasPending()
      when:
      instance.remove(idOne, keyTwo)
      then:
      !instance.hasPending()
      when:
      instance.add(idOne, keyOne)
      instance.add(idTwo, keyTwo)
      then:
      instance.hasPending()
      when:
      instance.remove(idOne)
      then:
      instance.hasPending()
      when:
      instance.remove(idTwo)
      then:
      !instance.hasPending()
   }
}
