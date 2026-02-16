/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class SpanId
{
    // java:S2245 - pseudo randomness is fine here
    @SuppressWarnings({"java:S2245", "java:S2119"})
    private static final Random RANDOM = new Random();
    private static final int ID_LENGTH = 8;
    private final byte[] id;

    private SpanId(byte[] bytes)
    {
        this.id = bytes.clone();
    }

    public static SpanId of(byte[] bytes)
    {
        Objects.requireNonNull(bytes, "bytes can not be null");
        if (bytes.length != ID_LENGTH)
        {
            throw new IllegalArgumentException("SpanId must be exactly " + ID_LENGTH + " # of bytes");
        }
        return new SpanId(bytes);
    }

    public static SpanId of()
    {
        byte[] bytes = new byte[ID_LENGTH];
        RANDOM.nextBytes(bytes);
        return new SpanId(bytes);
    }

    public static SpanId of(long value)
    {
        byte[] bytes = new byte[ID_LENGTH];
        for (int i = ID_LENGTH - 1; i >= 0; i--)
        {
            bytes[i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
        return new SpanId(bytes);
    }

    public byte[] getId()
    {
        return id.clone();
    }

    public String asHex()
    {
        return String.format("%016x", asUnsignedLong());
    }

    public long asUnsignedLong()
    {
        long value = 0;
        for (byte b : id)
        {
            value = (value << 8) | (b & 0xFFL);
        }
        return value;
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
        return Arrays.equals(id,traceId1.id);
    }
    @Override
    public int hashCode()
    {
        return Arrays.hashCode(id);
    }
    @Override
    public String toString()
    {
        return "SpanId{" +
                "spanId=" + asUnsignedLong() +
                "hex=" + asHex() +
                '}';
    }
}
