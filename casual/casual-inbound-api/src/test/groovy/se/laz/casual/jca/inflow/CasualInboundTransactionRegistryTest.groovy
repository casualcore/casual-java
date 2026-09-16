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
      expect:
      instance.getPendingTransactionCount() == 0
      when:
      instance.add(idOne, keyOne)
      instance.add(idOne, keyTwo)
      instance.add(idOne, keyOne)
      then:
      instance.getPendingTransactionCount() == 2
      instance.hasPending()
      when:
      instance.remove(idOne, keyOne)
      then:
      instance.getPendingTransactionCount() == 1
      instance.hasPending()
      when:
      instance.remove(idOne, keyTwo)
      then:
      !instance.hasPending()
      instance.getPendingTransactionCount() == 0
      when:
      instance.add(idOne, keyOne)
      instance.add(idTwo, keyTwo)
      instance.add(idTwo, keyOne)
      then:
      instance.getPendingTransactionCount() == 3
      instance.hasPending()
      when:
      instance.remove(idOne)
      then:
      instance.getPendingTransactionCount() == 2
      instance.hasPending()
      when:
      instance.remove(idTwo)
      then:
      !instance.hasPending()
      instance.getPendingTransactionCount() == 0
   }
}
