/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Random;

/**
 * SpanId represents a unique identifier for a span in a distributed tracing system.
 * It is used to correlate events across different services and processes.
 *
 * Consists of a random 64-bit identifier - guaranteed to be all non-zero.
 * Can produce a 16-hex-character lowercase string representation.
 */
public class SpanId
{
    // java:S2245 - pseudo randomness is fine here
    @SuppressWarnings({"java:S2245", "java:S2119"})
    private static final Random RANDOM = new Random();
    private static final int ID_LENGTH = 8;
    private static final byte[] ALL_ZERO = new byte[ID_LENGTH];
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
        do
        {
            RANDOM.nextBytes(bytes);
        }
        while(Arrays.equals(bytes, ALL_ZERO));
        // since all zeroes is not a valid span id
        return new SpanId(bytes);
    }

    public byte[] getId()
    {
        return id.clone();
    }

    public String asHex()
    {
        return HexFormat.of().formatHex(id);
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
                "id=" + asHex() +
                '}';
    }
}
