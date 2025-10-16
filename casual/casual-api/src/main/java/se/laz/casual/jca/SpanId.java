package se.laz.casual.jca;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

public class SpanId
{
    private static final int RADIX = 64;
    private final long spanId;

    private SpanId(long spanId)
    {
        this.spanId = spanId;
    }
    public static SpanId of()
    {
        Random random = new Random();
        BigInteger value = new BigInteger(RADIX, random);
        return new SpanId(value.longValue());
    }
    public static SpanId of(long spanId)
    {
        return new SpanId(spanId);
    }
    public String asHex()
    {
        return String.format("%016x", spanId);
    }

    public long getSpanId()
    {
        return spanId;
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
        return spanId == traceId1.spanId;
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(spanId);
    }
    @Override
    public String toString()
    {
        return "SpanId{" +
                "spanId=" + spanId +
                "hex=" + asHex() +
                '}';
    }
}
