/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util;

import java.util.Objects;

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
        return new Pair<>(first, second);
    }
    public K first()
    {
        return first;
    }
    public V second()
    {
        return second;
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
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
            Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(first, second);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Pair{");
        sb.append("first=").append(first);
        sb.append(", second=").append(second);
        sb.append('}');
        return sb.toString();
    }
}
