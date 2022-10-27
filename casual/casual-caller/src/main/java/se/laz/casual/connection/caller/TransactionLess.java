/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;

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
        try
        {
            Optional<Transaction> currentTransaction = Optional.ofNullable(transactionManager.suspend());
            ServiceReturn<CasualBuffer> value = supplier.get();
            currentTransaction.ifPresent(this::resumeTransaction);
            return value;
        }
        catch (SystemException e)
        {
            throw new CasualCallerException("Failed suspending current transaction", e);
        }
    }

    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(Supplier<CompletableFuture<ServiceReturn<CasualBuffer>>> supplier)
    {
        try
        {
            Optional<Transaction> currentTransaction = Optional.ofNullable(transactionManager.suspend());
            CompletableFuture<ServiceReturn<CasualBuffer>> value = supplier.get();
            currentTransaction.ifPresent(this::resumeTransaction);
            return value;
        }
        catch (SystemException e)
        {
            throw new CasualCallerException("Failed suspending current transaction", e);
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
