package se.kodarkatten.casual.network.utils;

import java.util.List;

/**
 * Created by aleph on 2017-03-03.
 */
public final class ByteUtils
{
    private ByteUtils()
    {}

    // We suppress this since it is bogus
    @SuppressWarnings("squid:AssignmentInSubExpressionCheck")
    public static long sumNumberOfBytes(List<byte[]> l)
    {
        return l.stream()
                .map(b -> (long)b.length)
                .reduce(0l,(sum, v) -> sum += v);
    }
}
