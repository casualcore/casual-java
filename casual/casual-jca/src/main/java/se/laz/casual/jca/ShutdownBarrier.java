/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import java.util.Objects;
import java.util.logging.Logger;

public class ShutdownBarrier
{
    private static final Logger log = Logger.getLogger(ShutdownBarrier.class.getName());
    private static final long NO_TIMEOUT = -1;
    private final long sleepTime;
    private long timeout;
    private final Predicate predicate;

    public ShutdownBarrier(long sleepTime, long timeout, Predicate predicate)
    {
        this.sleepTime = sleepTime;
        this.timeout = timeout;
        this.predicate = predicate;
    }

    /**
     * @param sleepTime How long current thread should sleep, in milliseconds
     * @param timeout Timeout, in milliseconds - if accumulated sleep > timeout, the spin lock returns
     * @param predicate Predicate, intermittent wait will happen until predicate returns true or, if timeout is set and triggered
     * @return the shutdown barrier
     */
    public static ShutdownBarrier of(long sleepTime, long timeout, Predicate predicate)
    {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return new ShutdownBarrier(sleepTime, timeout, predicate);
    }

    public static ShutdownBarrier of(long sleepTime, Predicate predicate)
    {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return new ShutdownBarrier(sleepTime, NO_TIMEOUT, predicate);
    }

    public void intermittentSleep()
    {
        boolean doCheckTimeout = timeout != NO_TIMEOUT;
        while (predicate.eval() && !hasTimedOut(doCheckTimeout))
        {
            try
            {
                Thread.sleep(sleepTime);
                if(doCheckTimeout)
                {
                    timeout -= sleepTime;
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                // we are not allowed to throw here, so we log it
                log.warning(() -> "thread interrupted during spinlock");
            }
        }
    }

    private boolean hasTimedOut(boolean doCheckTimeout)
    {
        return doCheckTimeout && timeout < 0;
    }

}
