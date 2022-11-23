/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.conversation.TpConnectReturn;

import javax.inject.Inject;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TransactionLess
{
    private static final String FAILED_SUSPENDING_TRANS = "Failed suspending current transaction";
    private TransactionManager transactionManager;

    // NOP constructor needed for WLS
    public TransactionLess()
    {}

    @Inject
    public TransactionLess(TransactionManagerProvider transactionManagerProvider)
    {
        transactionManager = transactionManagerProvider.getTransactionManager();
    }

    public ServiceReturn<CasualBuffer> tpcall(Supplier<ServiceReturn<CasualBuffer>> supplier)
    {
        return doWrap(supplier);
    }

    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(Supplier<CompletableFuture<ServiceReturn<CasualBuffer>>> supplier)
    {
        return doWrap(supplier);
    }

    public TpConnectReturn tpconnect(Supplier<TpConnectReturn> supplier)
    {
        return doWrap(supplier);
    }

    private <T> T doWrap(Supplier<T> supplier)
    {
        try
        {
            Optional<Transaction> currentTransaction = Optional.ofNullable(transactionManager.suspend());
            T value = supplier.get();
            currentTransaction.ifPresent(this::resumeTransaction);
            return value;
        }
        catch (SystemException e)
        {
            throw new CasualCallerException(FAILED_SUSPENDING_TRANS, e);
        }
    }
    
    private void resumeTransaction(Transaction transaction)
    {
        try
        {
            transactionManager.resume(transaction);
        }
        catch (InvalidTransactionException | SystemException e)
        {
            throw new CasualCallerException("Failed resuming transaction", e);
        }
    }

}
