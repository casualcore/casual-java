/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import se.laz.casual.network.JEEConcurrencyFactory;
import se.laz.casual.network.reverse.inbound.ReverseInboundListener;
import se.laz.casual.network.reverse.inbound.ReverseInboundServer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AutoReconnect implements Runnable
{
    private final ReverseInboundConnectionInformation reverseInboundConnectionInformation;
    private final ReverseInboundListener eventListener;
    private final StaggeredOptions staggeredOptions;

    private AutoReconnect(ReverseInboundConnectionInformation reverseInboundConnectionInformation,
                          ReverseInboundListener eventListener,
                          StaggeredOptions staggeredOptions)
    {
        this.reverseInboundConnectionInformation = reverseInboundConnectionInformation;
        this.eventListener = eventListener;
        this.staggeredOptions = staggeredOptions;
        JEEConcurrencyFactory.getManagedScheduledExecutorService().schedule(this, staggeredOptions.getNext().toMillis(), TimeUnit.MILLISECONDS);
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
    public void run()
    {
        try
        {
            ReverseInboundServer server = ReverseInboundServerImpl.of(reverseInboundConnectionInformation, eventListener);
            eventListener.connected(server);
        }
        catch(Exception e)
        {
            JEEConcurrencyFactory.getManagedScheduledExecutorService().schedule(this, staggeredOptions.getNext().toMillis(), TimeUnit.MILLISECONDS);
        }
    }
}