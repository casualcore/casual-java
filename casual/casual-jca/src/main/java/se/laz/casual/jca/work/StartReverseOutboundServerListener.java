/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.work;

import jakarta.resource.spi.work.WorkEvent;
import jakarta.resource.spi.work.WorkListener;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Work Listener to handle completion of {@link jakarta.resource.spi.work.Work} item by
 * {@link jakarta.resource.spi.work.WorkManager} to log if anything goes wrong with starting reverse outbound
 */
public class StartReverseOutboundServerListener implements WorkListener
{
    private static final Logger log = Logger.getLogger( StartReverseOutboundServerListener.class.getName());
    private StartReverseOutboundServerListener()
    {}

    public static StartReverseOutboundServerListener of()
    {
        return new StartReverseOutboundServerListener();
    }

    @Override
    public void workAccepted(WorkEvent e)
    {
        logWorkEvent( e, Level.FINEST, ()->"Casual reverse outbound start, work accepted." );
    }

    @Override
    public void workRejected(WorkEvent e)
    {
        logWorkEvent( e, Level.WARNING, ()-> "Casual reverse outbound start, work rejected, reverse outbound will not be started!!!"  );
    }

    @Override
    public void workStarted(WorkEvent e)
    {
        logWorkEvent( e, Level.FINEST, ()-> "Casual reverse outbound start, work started." );
    }

    @Override
    public void workCompleted(WorkEvent e)
    {
        logWorkEvent( e, Level.FINEST, ()-> "Casual reverse outbound start, work completed." );
    }

    private void logWorkEvent(WorkEvent e, Level level, Supplier<String> supplier )
    {
        log.log( level, supplier );
        if( e.getException() != null )
        {
            log.log(Level.SEVERE, e.getException(), () -> "Casual reverse outbound start WorkEvent contained an exception: ");
        }
    }
}
