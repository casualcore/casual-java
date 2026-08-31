/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.work;

import jakarta.resource.spi.work.WorkEvent;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WorkEventLogger
{
    private WorkEventLogger(){}

    public static void logWorkEvent(Logger log, WorkEvent e, Level level, Supplier<String> standardMessage,  Supplier<String> exceptionMessage)
    {
        log.log( level, standardMessage );
        if( e.getException() != null )
        {
            log.log(Level.SEVERE, e.getException(), exceptionMessage );
        }
    }
}
