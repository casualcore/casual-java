package se.kodarkatten.casual.network.utils;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import java.util.concurrent.TimeUnit;

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
            throw new RuntimeException(e);
        }
    }

}
