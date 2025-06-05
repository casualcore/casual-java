/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import java.util.Objects;

public class ShutdownBarrier
{
    private static final System.Logger log = System.getLogger(ShutdownBarrier.class.getName());
    private final long sleepTime;
    private final Predicate predicate;

    private ShutdownBarrier(long sleepTime, Predicate predicate)
    {
        this.sleepTime = sleepTime;
        this.predicate = predicate;
    }

    /**
     * Note: Shutdown barrier is not allowed to throw
     * @param sleepTime How long current thread should sleep, in milliseconds
     * @param predicate Predicate, intermittent sleep will happen until predicate returns true
     * @return the shutdown barrier
     */
    public static ShutdownBarrier of(long sleepTime, Predicate predicate)
    {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return new ShutdownBarrier(sleepTime, predicate);
    }

    public void intermittentSleep()
    {
        while (predicate.eval())
        {
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                // we are not allowed to throw here, so we log it
                log.log(System.Logger.Level.WARNING,() -> "shutdown barrier thread interrupted during sleep");
            }
        }
    }
}
