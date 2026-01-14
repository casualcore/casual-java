/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;
import java.util.function.Supplier;

import static java.lang.System.Logger.Level.*;

/**
 * Work Listener to handle completion of {@link jakarta.resource.spi.work.Work} item by
 * {@link jakarta.resource.spi.work.WorkManager} to log if anything goes wrong with starting the inbound server
 */
public class StartInboundServerListener implements WorkListener
{
    private static System.Logger log = System.getLogger( StartInboundServerListener.class.getName());
    private StartInboundServerListener()
    {
    }

    public static StartInboundServerListener of()
    {
        return new StartInboundServerListener();
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        logWorkEvent( e, DEBUG, ()->"Casual inbound start, work accepted." );
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        logWorkEvent( e, WARNING, ()-> "Casual inbound start, work rejected, inbound will not be started!!!"  );
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        logWorkEvent( e, DEBUG, ()-> "Casual inbound start, work started." );
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        logWorkEvent( e, DEBUG, ()-> "Casual inbound start, work completed." );
    }

    private void logWorkEvent( WorkEvent e, System.Logger.Level level, Supplier<String> supplier )
    {
        log.log( level, supplier );
        if( e.getException() != null )
        {
            log.log(ERROR, () -> "Casual inbound start WorkEvent contained an exception: ");
        }
    }
}
