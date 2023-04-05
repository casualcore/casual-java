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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RepeatUntilSuccessTaskWork<T> implements Work
{
    private static final Logger LOG = Logger.getLogger(RepeatUntilSuccessTaskWork.class.getName());
    private final Supplier<T> supplier;
    private final Consumer<T> consumer;
    private final Supplier<WorkManager> workManagerSupplier;

    private RepeatUntilSuccessTaskWork(Supplier<T> supplier, Consumer<T> consumer, Supplier<WorkManager> workManagerSupplier)
    {
        this.supplier = supplier;
        this.consumer = consumer;
        this.workManagerSupplier = workManagerSupplier;
    }

    public static <T> RepeatUntilSuccessTaskWork<T> of(Supplier<T> supplier, Consumer<T> consumer, Supplier<WorkManager> workManagerSupplier)
    {
        Objects.requireNonNull(supplier, "supplier can not be null");
        Objects.requireNonNull(consumer, "consumer can not be null");
        Objects.requireNonNull(workManagerSupplier, "workManagerSupplier can not be null");
        return  new RepeatUntilSuccessTaskWork<>(supplier, consumer, workManagerSupplier);
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
            LOG.warning(() -> "task failed: " + e);
            scheduleWork();
        }
    }

    @Override
    public String toString()
    {
        return "RepeatUntilSuccessTaskWork{" +
                "supplier=" + supplier +
                ", consumer=" + consumer +
                ", workManagerSupplier=" + workManagerSupplier +
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
