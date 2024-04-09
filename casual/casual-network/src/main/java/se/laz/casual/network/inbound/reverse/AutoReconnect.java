/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import se.laz.casual.api.util.work.RepeatUntilSuccessTaskWork;
import se.laz.casual.network.outbound.JEEConcurrencyFactory;
import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import javax.resource.spi.work.WorkManager;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoReconnect
{
   private final RepeatUntilSuccessTaskWork<ReverseInboundServer> task;
   public AutoReconnect(RepeatUntilSuccessTaskWork<ReverseInboundServer> task)
   {
      this.task = task;
      task.start();
   }

   public static AutoReconnect of(ReverseInboundConnectionInformation reverseInboundConnectionInformation,
                                  ReverseInboundListener eventListener,
                                  Supplier<WorkManager> workManagerSupplier)
   {
      Objects.requireNonNull(reverseInboundConnectionInformation,"reverseInboundConnectionInformation can not be null");
      Objects.requireNonNull(eventListener, "eventListener can not be null");
      Objects.requireNonNull(workManagerSupplier, "workManagerSupplier can not be null");
      Supplier<ReverseInboundServer> supplier = () -> ReverseInboundServerImpl.of(reverseInboundConnectionInformation, eventListener, workManagerSupplier);
      Consumer<ReverseInboundServer> consumer = eventListener::connected;
      RepeatUntilSuccessTaskWork<ReverseInboundServer> task = RepeatUntilSuccessTaskWork.of(
              supplier,
              consumer,
              workManagerSupplier,
              reverseInboundConnectionInformation.getMaxBackoffMillis(),
              JEEConcurrencyFactory.getManagedScheduledExecutorService());
      return new AutoReconnect(task);
   }

   @Override
   public String toString()
   {
      return "AutoReconnect{" +
              "task=" + task +
              '}';
   }
}
