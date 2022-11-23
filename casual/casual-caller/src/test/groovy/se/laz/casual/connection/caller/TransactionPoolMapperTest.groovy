/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller

import se.laz.casual.api.CasualRuntimeException
import spock.lang.Shared
import spock.lang.Specification

import javax.transaction.Status
import javax.transaction.Transaction

class TransactionPoolMapperTest extends Specification
{
    @Shared
    TransactionManagerImpl transactionManager

    def setup()
    {
        TransactionPoolMapper.resetForTest()
        TransactionPoolMapper.getInstance().setActiveForTest(true)
        TransactionPoolMapper.getInstance().setTransactionManager(transactionManager = new TransactionManagerImpl())
    }

    def cleanupSpec()
    {
        TransactionPoolMapper.resetForTest()
    }

    def "Test default config state, should be disabled/inactive"()
    {
        setup:
        TransactionPoolMapper.resetForTest()

        expect:
        !TransactionPoolMapper.getInstance().isPoolMappingActive()
    }

    def "Test with single transaction"()
    {
        setup:
        Transaction transaction = new TransactionImpl(Status.STATUS_ACTIVE)
        transactionManager.setCurrentTransaction(transaction)

        String actualPoolName = "hello, world!"
        TransactionPoolMapper.getInstance().setPoolNameForCurrentTransaction(actualPoolName)

        expect:
        TransactionPoolMapper.getInstance().getPoolNameForCurrentTransaction() == actualPoolName
        TransactionPoolMapper.getInstance().getNumberOfTrackedTransactions() == 1
    }

    def "Test with many different transaction instances"()
    {
        setup:
        int transactions = 5

        for (int i = 0; i < transactions; i++)
        {
            transactionManager.setCurrentTransaction(new TransactionImpl(Status.STATUS_ACTIVE))
            TransactionPoolMapper.getInstance().setPoolNameForCurrentTransaction("hello transaction " + i)
        }

        expect:
        TransactionPoolMapper.getInstance().getNumberOfTrackedTransactions() == transactions
    }

    def "Should throw exception if attempting to set sticky for an already stickied transaction"()
    {
        given:
        transactionManager.setCurrentTransaction(new TransactionImpl(Status.STATUS_ACTIVE))

        TransactionPoolMapper.getInstance().setPoolNameForCurrentTransaction("eis/myPool")

        when:
        TransactionPoolMapper.getInstance().setPoolNameForCurrentTransaction("eis/whateverPool")

        then:
        def e = thrown(CasualRuntimeException)
        System.err.println(e.message)
        e.message.contains("already stickied")
    }
}
