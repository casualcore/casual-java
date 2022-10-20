package se.laz.casual.api.util;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class RepeatUntilSuccessTask<T> implements Runnable
{
    private static final Logger LOG = Logger.getLogger(RepeatUntilSuccessTask.class.getName());
   private final Supplier<T> supplier;
   private final Consumer<T> consumer;
   private final StaggeredOptions staggeredOptions;
   private final Supplier<ScheduledExecutorService> executorServiceSupplier;

   private RepeatUntilSuccessTask(Supplier<T> supplier, Consumer<T> consumer, StaggeredOptions staggeredOptions, Supplier<ScheduledExecutorService> executorServiceSupplier)
   {
      this.supplier = supplier;
      this.consumer = consumer;
      this.staggeredOptions = staggeredOptions;
      this.executorServiceSupplier = executorServiceSupplier;
   }

   public static <T> RepeatUntilSuccessTask<T> of(Supplier<T> supplier, Consumer<T> consumer, StaggeredOptions staggeredOptions, Supplier<ScheduledExecutorService> executorServiceSupplier)
   {
      Objects.requireNonNull(supplier, "supplier can not be null");
      Objects.requireNonNull(consumer, "consumer can not be null");
      Objects.requireNonNull(executorServiceSupplier, "executorServiceSupplier can not be null");
      return  new RepeatUntilSuccessTask<>(supplier, consumer, staggeredOptions, executorServiceSupplier);
   }

   public void start()
   {
      executorServiceSupplier.get().schedule(this, staggeredOptions.getNext().toMillis(), TimeUnit.MILLISECONDS);
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
         executorServiceSupplier.get().schedule(this, staggeredOptions.getNext().toMillis(), TimeUnit.MILLISECONDS);
      }
   }

   @Override
   public String toString()
   {
      return "RepeatUntilSuccessTask{" +
              "supplier=" + supplier +
              ", consumer=" + consumer +
              ", staggeredOptions=" + staggeredOptions +
              '}';
   }
}
