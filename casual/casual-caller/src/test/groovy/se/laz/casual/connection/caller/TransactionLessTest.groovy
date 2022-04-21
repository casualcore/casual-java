/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller

import se.laz.casual.api.buffer.CasualBuffer
import se.laz.casual.api.buffer.ServiceReturn
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.ServiceReturnState
import spock.lang.Specification

import javax.transaction.Transaction
import javax.transaction.TransactionManager
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class TransactionLessTest extends Specification
{
   TransactionManagerProvider transactionManagerProvider
   TransactionLess instance
   Transaction transaction
   TransactionManager transactionManager

   def setup()
   {
      transaction = Mock(Transaction)
      transactionManager = Mock(TransactionManager)
      transactionManagerProvider = Mock(TransactionManagerProvider){
         getTransactionManager() >> {
            return transactionManager
         }
      }
      instance = new TransactionLess(transactionManagerProvider)
   }

   def 'tpcall in transaction'()
   {
      given:
      ServiceReturn<CasualBuffer> result = new ServiceReturn<>(Mock(CasualBuffer), ServiceReturnState.TPSUCCESS, ErrorState.OK, 0)
      Supplier<ServiceReturn<CasualBuffer>> supplier = {result}
      1 * transactionManager.suspend() >> {
         transaction
      }
      when:
      def actualResult = instance.tpcall(supplier)
      then:
      1 * transactionManager.resume(transaction)
      actualResult == result
   }

   def 'tpcall no transaction'()
   {
      given:
      ServiceReturn<CasualBuffer> result = new ServiceReturn<>(Mock(CasualBuffer), ServiceReturnState.TPSUCCESS, ErrorState.OK, 0)
      Supplier<ServiceReturn<CasualBuffer>> supplier = {result}
      1 * transactionManager.suspend() >> {
         null
      }
      when:
      def actualResult = instance.tpcall(supplier)
      then:
      0 * transactionManager.resume(transaction)
      actualResult == result
   }

   def 'tpacall in transaction'()
   {
      given:
      CompletableFuture<ServiceReturn<CasualBuffer>> futureResult = new CompletableFuture<>()
      Supplier<ServiceReturn<CasualBuffer>> supplier = {futureResult}
      1 * transactionManager.suspend() >> {
         transaction
      }
      when:
      def actualResult = instance.tpacall(supplier)
      then:
      1 * transactionManager.resume(transaction)
      actualResult == futureResult
   }

   def 'tpacall no transaction'()
   {
      given:
      CompletableFuture<ServiceReturn<CasualBuffer>> futureResult = new CompletableFuture<>()
      Supplier<ServiceReturn<CasualBuffer>> supplier = {futureResult}
      1 * transactionManager.suspend() >> {
         null
      }
      when:
      def actualResult = instance.tpacall(supplier)
      then:
      0 * transactionManager.resume(transaction)
      actualResult == futureResult
   }

}
