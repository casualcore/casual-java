/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util.work;

import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkManager;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RepeatUntilSuccessTaskWork<T> implements Work
{
    private static final Logger LOG = Logger.getLogger(RepeatUntilSuccessTaskWork.class.getName());
    private final Supplier<T> supplier;
    private final Consumer<T> consumer;
    private final Supplier<WorkManager> workManagerSupplier;
    private final BackoffHelper backoffHelper;
    private final ScheduledExecutorService backoffScheduler;

    private RepeatUntilSuccessTaskWork(Supplier<T> supplier, Consumer<T> consumer, Supplier<WorkManager> workManagerSupplier,
                                       long maxBackoffMillis, ScheduledExecutorService backoffScheduler)
    {
        this.supplier = supplier;
        this.consumer = consumer;
        this.workManagerSupplier = workManagerSupplier;
        this.backoffHelper = BackoffHelper.of(maxBackoffMillis);
        this.backoffScheduler = backoffScheduler;
    }

    public static <T> RepeatUntilSuccessTaskWork<T> of(Supplier<T> supplier, Consumer<T> consumer, Supplier<WorkManager> workManagerSupplier,
                                                       long maxBackoffMillis, ScheduledExecutorService backoffScheduler)
    {
        Objects.requireNonNull(supplier, "supplier can not be null");
        Objects.requireNonNull(consumer, "consumer can not be null");
        Objects.requireNonNull(workManagerSupplier, "workManagerSupplier can not be null");
        Objects.requireNonNull(backoffScheduler, "backoffScheduler can not be null");
        return new RepeatUntilSuccessTaskWork<>(supplier, consumer, workManagerSupplier, maxBackoffMillis, backoffScheduler);
    }

    public void start()
    {
        scheduleWork();
    }

    @Override
    public void run()
    {
        try
        {
            consumer.accept(supplier.get());
        }
        catch(Exception e)
        {
            long currentBackoff = backoffHelper.registerFailure();
            LOG.warning(() -> "task failed: failure #" + backoffHelper.getFailures() + ", retrying in " + currentBackoff + " " + e);
            backoffScheduler.schedule(this::scheduleWork, currentBackoff, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public String toString()
    {
        return "RepeatUntilSuccessTaskWork{" +
                "supplier=" + supplier +
                ", consumer=" + consumer +
                ", workManagerSupplier=" + workManagerSupplier +
                ", backoff=" + backoffHelper +
                '}';
    }

    @Override
    public void release()
    {
        //NOP
    }

    private void scheduleWork()
    {
        try
        {
            workManagerSupplier.get().scheduleWork(this, WorkManager.INDEFINITE, null, RepeatUntilSuccessTaskWorkListener.of());
        }
        catch (WorkException e)
        {
            LOG.warning(() -> "failed to schedule work, will retry once: " + e);
            try
            {
                workManagerSupplier.get().scheduleWork(this, WorkManager.INDEFINITE, null, RepeatUntilSuccessTaskWorkListener.of());
            }
            catch (WorkException ee)
            {
                throw new CasualWorkException("fatality - failed retry scheduling work!", ee);
            }
        }
    }
}
