/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.work;

import se.laz.casual.jca.DelayFailedException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Delayer
{
    private static final Logger log = Logger.getLogger(Delayer.class.getName());

    private Delayer()
    {}

    public static void delay(long seconds)
    {
        try
        {
            log.finest(() -> "delaying current thread by " + seconds + " seconds");
            Duration delay = Duration.ofSeconds(seconds);
            TimeUnit.SECONDS.sleep(delay.get(ChronoUnit.SECONDS));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new DelayFailedException("Interrupted while delaying something", e);
        }
    }
}
