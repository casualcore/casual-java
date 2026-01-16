/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

public class SpanId
{
    private static final int RADIX = 64;
    private final long id;

    private SpanId(long id)
    {
        this.id = id;
    }
    // java:S2245 - pseudo randomness is fine here
    // java:S2119 - same as having Random in the ctor
    @SuppressWarnings({"java:S2245", "java:S2119"})
    public static SpanId of()
    {
        Random random = new Random();
        BigInteger value = new BigInteger(RADIX, random);
        return new SpanId(value.longValue());
    }
    public static SpanId of(long id)
    {
        return new SpanId(id);
    }
    public String asHex()
    {
        return String.format("%016x", id);
    }

    public long getId()
    {
        return id;
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
        SpanId traceId1 = (SpanId) o;
        return id == traceId1.id;
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
    @Override
    public String toString()
    {
        return "SpanId{" +
                "spanId=" + id +
                "hex=" + asHex() +
                '}';
    }
}
