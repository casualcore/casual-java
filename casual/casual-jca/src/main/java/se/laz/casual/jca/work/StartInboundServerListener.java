/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Work Listener to handle completion of {@link javax.resource.spi.work.Work} item by
 * {@link javax.resource.spi.work.WorkManager} to log if anything goes wrong with starting the inbound server
 */
public class StartInboundServerListener implements WorkListener
{
    private static Logger log = Logger.getLogger( StartInboundServerListener.class.getName());
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
        logWorkEvent( e, Level.FINEST, ()->"Casual inbound start, work accepted." );
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        logWorkEvent( e, Level.WARNING, ()-> "Casual inbound start, work rejected, inbound will not be started!!!"  );
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        logWorkEvent( e, Level.FINEST, ()-> "Casual inbound start, work started." );
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        logWorkEvent( e, Level.FINEST, ()-> "Casual inbound start, work completed." );
    }

    private void logWorkEvent( WorkEvent e, Level level, Supplier<String> supplier )
    {
        log.log( level, supplier );
        if( e.getException() != null )
        {
            log.log(Level.SEVERE, e.getException(), () -> "Casual inbound start WorkEvent contained an exception: ");
        }
    }
}
