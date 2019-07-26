/*
 * Copyright (c) 2017 - 2019, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.util.time;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class InstantUtil
{
    private InstantUtil()
    {

    }

    public static long toNanos(final Instant instant)
    {
        Objects.requireNonNull(instant, "instant can not be null");
        return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    }

    public static Instant fromNanos(long nanos)
    {
        return Instant.ofEpochSecond( 0L, nanos);
    }
}
