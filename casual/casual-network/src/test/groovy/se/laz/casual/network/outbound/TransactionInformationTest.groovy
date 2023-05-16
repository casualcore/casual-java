package se.laz.casual.network.outbound

import spock.lang.Specification

import javax.transaction.Transaction
import javax.transaction.TransactionManager
import java.util.logging.Level
import java.util.logging.Logger

class TransactionInformationTest extends Specification
{
   TransactionInformation instance
   TransactionManager transactionManager

   def setup()
   {
      instance = TransactionInformation.of()
      // note:
      // we do this since the logging in addCurrentTransaction also gets the current status
      Logger.getLogger(TransactionInformation.class.getName()).setLevel(Level.OFF)
   }

   def 'normal sequence'()
   {
      given:
      Transaction transaction = Mock(Transaction){
         getStatus() >>> [TransactionStatus.STATUS_ACTIVE.getValue(), TransactionStatus.STATUS_PREPARING.getValue(), TransactionStatus.STATUS_COMMITTING.getValue(), TransactionStatus.STATUS_COMMITTED.getValue()]
      }
      transactionManager = Mock(TransactionManager){
         getTransaction() >> transaction
      }
      instance.setTransactionManager(transactionManager)
      instance.addCurrentTransaction()
      when:
      boolean inFlight = instance.transactionsInFlight()
      then:
      inFlight == true
      when:
      inFlight = instance.transactionsInFlight()
      then:
      inFlight == true
      when:
      inFlight = instance.transactionsInFlight()
      then:
      inFlight == true
      when:
      inFlight = instance.transactionsInFlight()
      then:
      inFlight == false
   }

   def 'one phase'()
   {
      given:
      Transaction transaction = Mock(Transaction){
         getStatus() >>> [TransactionStatus.STATUS_ACTIVE.getValue(), TransactionStatus.STATUS_PREPARING.getValue(), TransactionStatus.STATUS_PREPARED.getValue()]
      }
      transactionManager = Mock(TransactionManager){
         getTransaction() >> transaction
      }
      instance.setTransactionManager(transactionManager)
      instance.addCurrentTransaction()
      when:
      boolean inFlight = instance.transactionsInFlight()
      then:
      inFlight == true
      when:
      inFlight = instance.transactionsInFlight()
      then:
      inFlight == true
      when:
      inFlight = instance.transactionsInFlight()
      then:
      inFlight == false
   }

   def 'rollback'()
   {
      given:
      Transaction transaction = Mock(Transaction){
         getStatus() >>> [TransactionStatus.STATUS_ACTIVE.getValue(), TransactionStatus.STATUS_PREPARING.getValue(), TransactionStatus.STATUS_ROLLING_BACK.getValue(), TransactionStatus.STATUS_ROLLEDBACK.getValue()]
      }
      transactionManager = Mock(TransactionManager){
         getTransaction() >> transaction
      }
      instance.setTransactionManager(transactionManager)
      instance.addCurrentTransaction()
      when:
      boolean inFlight = instance.transactionsInFlight()
      then:
      inFlight == true
      when:
      inFlight = instance.transactionsInFlight()
      then:
      inFlight == true
      when:
      inFlight = instance.transactionsInFlight()
      then:
      inFlight == true
      when:
      inFlight = instance.transactionsInFlight()
      then:
      inFlight == false
   }

}
