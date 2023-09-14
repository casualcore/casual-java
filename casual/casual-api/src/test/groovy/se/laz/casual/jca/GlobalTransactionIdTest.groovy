package se.laz.casual.jca

import spock.lang.Specification
import spock.lang.Unroll

class GlobalTransactionIdTest extends Specification
{
   @Unroll
   def 'match equality #instanceOne #instanceTwo'()
   {
      expect:
      expression(instanceOne, instanceTwo)
      where:
      instanceOne                              || instanceTwo                              || expression
      GlobalTransactionId.of('abc' as byte[])  || GlobalTransactionId.of('abc' as byte[])  || { id1, id2 -> id1 == id2}
      GlobalTransactionId.of('abc' as byte[])  || GlobalTransactionId.of('abd' as byte[])  || { id1, id2 -> id1 != id2}
   }
}
