/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import se.laz.casual.api.util.work.RepeatUntilSuccessTaskWork;
import se.laz.casual.network.outbound.JEEConcurrencyFactory;
import se.laz.casual.network.reverse.inbound.ReverseInboundConnectListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import javax.resource.spi.work.WorkManager;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoConnect
{
   private final RepeatUntilSuccessTaskWork<ReverseInboundServer> task;
   private AutoConnect(RepeatUntilSuccessTaskWork<ReverseInboundServer> task)
   {
      this.task = task;
      task.start();
   }

   public static AutoConnect of(ReverseInboundConnectionInformation reverseInboundConnectionInformation,
                                ReverseInboundConnectListener connectListener,
                                ReverseInboundListener eventListener,
                                Supplier<WorkManager> workManagerSupplier)
   {
      Objects.requireNonNull(reverseInboundConnectionInformation, "reverseInboundConnectionInformation can not be null");
      Objects.requireNonNull(connectListener, "connectListener can not be null");
      Objects.requireNonNull(eventListener, "eventListener can not be null");
      Objects.requireNonNull(workManagerSupplier, "workManagerSupplier can not be null");
      Supplier<ReverseInboundServer> supplier = () -> ReverseInboundServerImpl.of(reverseInboundConnectionInformation, eventListener, workManagerSupplier);
      Consumer<ReverseInboundServer> consumer = server -> {
         connectListener.connected(server);
         eventListener.connected(server);
      };
      RepeatUntilSuccessTaskWork<ReverseInboundServer> task = RepeatUntilSuccessTaskWork.of(
              supplier,
              consumer,
              workManagerSupplier,
              reverseInboundConnectionInformation.getMaxBackoffMillis(),
              JEEConcurrencyFactory.getManagedScheduledExecutorService());
      return new AutoConnect(task);
   }

    @Override
    public String toString()
    {
        return "AutoConnect{" +
                "task=" + task +
                '}';
    }
}
