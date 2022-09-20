/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import se.laz.casual.api.util.RepeatUntilSuccessTask;
import se.laz.casual.api.util.StaggeredOptions;
import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoReconnect
{
   private final RepeatUntilSuccessTask<ReverseInboundServer> task;
   public AutoReconnect(RepeatUntilSuccessTask<ReverseInboundServer> task)
   {
      this.task = task;
      task.start();
   }

   public static AutoReconnect of(ReverseInboundConnectionInformation reverseInboundConnectionInformation,
                                  ReverseInboundListener eventListener,
                                  StaggeredOptions staggeredOptions)
   {
      Objects.requireNonNull(reverseInboundConnectionInformation,"reverseInboundConnectionInformation can not be null");
      Objects.requireNonNull(eventListener, "eventListener can not be null");
      Objects.requireNonNull(staggeredOptions, "staggeredOptions can not be null");
      Supplier<ReverseInboundServer> supplier = () -> ReverseInboundServerImpl.of(reverseInboundConnectionInformation, eventListener);
      Consumer<ReverseInboundServer> consumer = eventListener::connected;
      RepeatUntilSuccessTask<ReverseInboundServer> task = RepeatUntilSuccessTask.of(supplier, consumer, staggeredOptions);
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
