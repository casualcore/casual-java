/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util.work;

import java.util.Objects;

/**
 * Helper class to aid in counting failures and calculating backoff times for
 * some specific task.
 * <p>
 * Uses a specific algorithm where backoff is incremented in 10 steps until some
 * configured maximum backoff where each step is progressively longer according
 * to 1/10th, 1/9th, 1/8th until 1/1 where the maximum backoff is reached. At
 * that point every failure will yield the maximum backoff that was selected.
 */
public class BackoffHelper
{
    private final long maxBackoffMillis;
    private long failures = 0;

    private BackoffHelper(long maxBackoffMillis)
    {
        this.maxBackoffMillis = maxBackoffMillis;
    }

    public static BackoffHelper of(long maxBackoffMillis)
    {
        return new BackoffHelper(maxBackoffMillis);
    }

    private long getCurrentBackoff()
    {
        final long failuresUntilMaxBackoffDelay = 10;

        // Backoff in steps of 1/10, 1/9 .. 1/2, 1/1 where maxBackoffMillis is reached
        if (failures < failuresUntilMaxBackoffDelay)
        {
            long number = failuresUntilMaxBackoffDelay - failures;
            return maxBackoffMillis / number;
        }
        else
        {
            return maxBackoffMillis;
        }
    }

    public long getMaxBackoffMillis()
    {
        return maxBackoffMillis;
    }

    public long getFailures()
    {
        return failures;
    }

    @Override
    public String toString()
    {
        return "Backoff{" +
                "maxBackoffMillis=" + maxBackoffMillis +
                ", failures=" + failures +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        BackoffHelper backoffHelper = (BackoffHelper) o;
        return maxBackoffMillis == backoffHelper.maxBackoffMillis && failures == backoffHelper.failures;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(maxBackoffMillis, failures);
    }

    /**
     * Records failure and returns the current backoff value to use. These are
     * the same operation to ensure correct order of backoff calculation and
     * incrementation of the failure counter.
     * @return the current backoff in milliseconds the use before retrying after the current failure
     */
    public long registerFailure()
    {
        long backoff = getCurrentBackoff();
        failures++;
        return backoff;
    }
}
