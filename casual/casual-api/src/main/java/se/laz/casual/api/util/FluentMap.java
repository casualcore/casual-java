/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util;

import java.util.Map;
import java.util.Objects;

public final class FluentMap<K, V>
{
    private final Map<K,V> m;
    private FluentMap(Map<K, V> m)
    {
        this.m = m;
    }

    public static <K,V> FluentMap<K, V> of(Map<K, V> m)
    {
        Objects.requireNonNull(m);
        return new FluentMap<>(m);
    }

    public FluentMap<K,V> put(K k, V v)
    {
        m.put(k, v);
        return this;
    }

    public Map<K, V> map()
    {
        return m;
    }

}
