/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.util

import se.laz.casual.api.xa.XID
import spock.lang.Specification

import javax.transaction.xa.Xid

class PrettyPrinterTest extends Specification
{
   def 'Xid'()
   {
      given:
      def gtridString = 'abababababababab'
      def bqualString = 'fefefefefefefefe'
      def format = 42
      def gtrid = new BigInteger(gtridString, 16).toByteArray()
      def bqual = new BigInteger(bqualString, 16).toByteArray()
      XID xid = XID.of(gtrid, bqual, format)
      def expected = "${gtridString}:${bqualString}:${format}"
      when:
      String asString = PrettyPrinter.casualStringify(xid)
      then:
      asString == expected
   }

   def 'Xid - null branch'()
   {
      given:
      def gtridString = 'abababababababab'
      def bqualString = null
      def format = 42
      def gtrid = new BigInteger(gtridString, 16).toByteArray()
      def bqual = null
      Xid xid = Mock(Xid){
         getGlobalTransactionId() >> {
            gtrid
         }
         getBranchQualifier() >> {
            bqual
         }
         getFormatId() >> {
            format
         }
      }
      def expected = "${gtridString}:${bqualString}:${format}"
      when:
      String asString = PrettyPrinter.casualStringify(xid)
      then:
      asString == expected
   }

   def 'uuid'()
   {
      given:
      def leastSignificantBits = -6640848550566316080
      def mostSignificantBits = 4634056117406941194
      def uuid = new UUID(mostSignificantBits, leastSignificantBits)
      def expected = '404f797c8b44400aa3d6f76d9053b7d0'
      when:
      def asString = PrettyPrinter.casualStringify(uuid)
      then:
      asString == expected
   }

}
