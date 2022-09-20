/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound.reverse;

import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;

public class StaggeredOptions
{
    private static final Logger LOG = Logger.getLogger(StaggeredOptions.class.getName());
    private final Duration initialDelay;
    private Duration subsequentDelay;
    private Duration maxDelay;
    private int staggerFactor;
    private boolean initial = true;

    private StaggeredOptions(Duration initialDelay, Duration subsequentDelay, Duration maxDelay, int staggerFactor)
    {
        this.initialDelay = initialDelay;
        this.subsequentDelay = subsequentDelay;
        this.maxDelay = maxDelay;
        this.staggerFactor = staggerFactor;
    }

    public static StaggeredOptions of(Duration initialDelay, Duration subsequentDelay, Duration maxDelay, int staggerFactor)
    {
        Objects.requireNonNull(initialDelay, "initialDelay can not be null");
        Objects.requireNonNull(subsequentDelay, "subsequentDelay can not be null");
        Objects.requireNonNull(maxDelay, "maxDelay can not be null");
        if(staggerFactor <= 0)
        {
            throw new IllegalArgumentException("staggerFactor equal to or below zero is not supported");
        }
        return new StaggeredOptions(initialDelay, subsequentDelay, maxDelay, staggerFactor);
    }

    public Duration getNext()
    {
        if(initial)
        {
            initial = false;
            return initialDelay;
        }
        subsequentDelay = Duration.ofMillis(initialDelay.toMillis() + subsequentDelay.toMillis() * staggerFactor);
        subsequentDelay = subsequentDelay.compareTo(maxDelay) == 1 ? maxDelay : subsequentDelay;
        LOG.finest(() -> " delay: " + subsequentDelay);
        return subsequentDelay;
    }

}
