package se.kodarkatten.casual.network.utils;

/**
 * Created by aleph on 2017-04-03.
 */
public final class Pair<K,V>
{
    private final K first;
    private final V second;
    private Pair(final K first, final V second)
    {
        this.first = first;
        this.second = second;
    }
    public static <K,V> Pair<K,V> of(final K first, final V second)
    {
        return new Pair(first, second);
    }
    public K first()
    {
        return first;
    }
    public V second()
    {
        return second;
    }
}


