package se.laz.casual.network.inbound.reverse;

import se.laz.casual.network.reverse.inbound.ReverseInboundConnectListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoConnect
{
   private final RepeatUntilSuccessTask<ReverseInboundServer> task;
   private AutoConnect(RepeatUntilSuccessTask<ReverseInboundServer> task)
   {
      this.task = task;
   }

   public static AutoConnect of(ReverseInboundConnectionInformation reverseInboundConnectionInformation,
                                ReverseInboundConnectListener connectListener,
                                ReverseInboundListener eventListener,
                                StaggeredOptions staggeredOptions)
   {
      Objects.requireNonNull(reverseInboundConnectionInformation, "reverseInboundConnectionInformation can not be null");
      Objects.requireNonNull(connectListener, "connectListener can not be null");
      Objects.requireNonNull(eventListener, "eventListener can not be null");
      Objects.requireNonNull(staggeredOptions, "staggeredOptions can not be null");
      Supplier<ReverseInboundServer> supplier = () -> ReverseInboundServerImpl.of(reverseInboundConnectionInformation, eventListener);
      Consumer<ReverseInboundServer> consumer = server -> {
         connectListener.connected(server);
         eventListener.connected(server);
      };
      RepeatUntilSuccessTask<ReverseInboundServer> task = RepeatUntilSuccessTask.of(supplier, consumer, staggeredOptions);
      return new AutoConnect(task);
   }
}
