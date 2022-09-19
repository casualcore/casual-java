/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoReconnect
{
    private final RepeatUntilSuccessTask<ReverseInboundServer> task;
    private AutoReconnect(ReverseInboundConnectionInformation reverseInboundConnectionInformation,
                          ReverseInboundListener eventListener,
                          StaggeredOptions staggeredOptions)
    {
       Supplier<ReverseInboundServer> supplier = () -> ReverseInboundServerImpl.of(reverseInboundConnectionInformation, eventListener);
       Consumer<ReverseInboundServer> consumer = eventListener::connected;
       this.task = RepeatUntilSuccessTask.of(supplier, consumer, staggeredOptions);
       this.task.start();
    }

    public static AutoReconnect of(ReverseInboundConnectionInformation reverseInboundConnectionInformation,
                          ReverseInboundListener eventListener,
                          StaggeredOptions staggeredOptions)
    {
       Objects.requireNonNull(reverseInboundConnectionInformation,"reverseInboundConnectionInformation can not be null");
       Objects.requireNonNull(eventListener, "eventListener can not be null");
       Objects.requireNonNull(staggeredOptions, "staggeredOptions can not be null");
       return new AutoReconnect(reverseInboundConnectionInformation, eventListener, staggeredOptions);
    }

   @Override
   public String toString()
   {
      return "AutoReconnect{" +
              "task=" + task +
              '}';
   }
}
