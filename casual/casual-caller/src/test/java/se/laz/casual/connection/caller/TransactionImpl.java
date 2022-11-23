/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class TransactionImpl implements Transaction
{
    private int status;

    public TransactionImpl(int status)
    {
        this.status = status;
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException
    {
        status = Status.STATUS_COMMITTED;
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException
    {
        return true;
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException
    {
        return true;
    }

    @Override
    public int getStatus() throws SystemException
    {
        return status;
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException
    {
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException
    {
        status = Status.STATUS_ROLLEDBACK;
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        status = Status.STATUS_MARKED_ROLLBACK;
    }
}
