/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.util.time;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    public static long toEpochMicro( Instant instant )
    {
        return toDurationMicro(Instant.EPOCH, instant);
    }

    public static long toDurationMicro( Instant start, Instant end )
    {
        // from ChronoUnit.between:
        // Implementations should perform any queries or calculations using the units available in ChronoUnit
        // or the fields available in ChronoField.
        // If the unit is not supported an UnsupportedTemporalTypeException must be thrown.
        // Implementations must not alter the specified temporal objects.
        //
        // On the platform what we currently support, this is not a problem
        // We also do not know of any platform where this does not work
        // The same goes for end below
        return ChronoUnit.MICROS.between(start, end);
    }
}
