/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.utils;

import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkListener;
import jakarta.resource.spi.work.WorkManager;

public class DummyWorkManager implements WorkManager
{
    private Thread t;

    private DummyWorkManager()
    {

    }

    public static DummyWorkManager of()
    {
        return new DummyWorkManager();
    }

    @Override
    public void doWork(Work work) throws WorkException
    {
        throw new WorkException("not implemented");
    }

    @Override
    public void doWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener) throws WorkException
    {
        throw new WorkException("not implemented");
    }

    @Override
    public long startWork(Work work) throws WorkException
    {
        if(null != t)
        {
            throw new WorkException("You can only dispatch one unit of work to this dummy implementation");
        }
        t = new Thread(work);
        t.start();
        return 0;
    }

    @Override
    public long startWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener) throws WorkException
    {
        return startWork( work );
    }

    @Override
    public void scheduleWork(Work work) throws WorkException
    {
        throw new WorkException("not implemented");
    }

    @Override
    public void scheduleWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener) throws WorkException
    {
        throw new WorkException("not implemented");
    }

    public void interruptWork()
    {
        t.interrupt();
    }

    public void done()
    {
        if(null == t)
        {
            return;
        }
        try
        {
            t.join();
            t = null;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}
