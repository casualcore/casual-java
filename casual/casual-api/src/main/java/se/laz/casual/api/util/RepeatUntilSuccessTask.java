package se.laz.casual.api.util;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RepeatUntilSuccessTask<T> implements Runnable
{
   private final Supplier<T> supplier;
   private final Consumer<T> consumer;
   private final StaggeredOptions staggeredOptions;

   private RepeatUntilSuccessTask(Supplier<T> supplier, Consumer<T> consumer, StaggeredOptions staggeredOptions)
   {
      this.supplier = supplier;
      this.consumer = consumer;
      this.staggeredOptions = staggeredOptions;
   }

   public static <T> RepeatUntilSuccessTask<T> of(Supplier<T> supplier, Consumer<T> consumer, StaggeredOptions staggeredOptions)
   {
      Objects.requireNonNull(supplier, "supplier can not be null");
      Objects.requireNonNull(consumer, "consumer can not be null");
      return  new RepeatUntilSuccessTask<>(supplier, consumer, staggeredOptions);
   }

   public void start()
   {
      JEEConcurrencyFactory.getManagedScheduledExecutorService().schedule(this, staggeredOptions.getNext().toMillis(), TimeUnit.MILLISECONDS);
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
         JEEConcurrencyFactory.getManagedScheduledExecutorService().schedule(this, staggeredOptions.getNext().toMillis(), TimeUnit.MILLISECONDS);
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
