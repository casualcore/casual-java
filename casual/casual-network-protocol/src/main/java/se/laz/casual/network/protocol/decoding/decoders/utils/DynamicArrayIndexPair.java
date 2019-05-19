/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.utils;

import java.util.List;

/**
 * Created by aleph on 2017-03-07.
 */
public class DynamicArrayIndexPair<T>
{
    private List<T> items;
    private int index;

    private DynamicArrayIndexPair(List<T> items, int index)
    {
        this.items = items;
        this.index = index;
    }
    public static <T> DynamicArrayIndexPair<T> of(List<T> names, int index)
    {
        return new DynamicArrayIndexPair<>(names, index);
    }
    public List<T> getBytes()
    {
        return items;
    }
    public int getIndex()
    {
        return index;
    }
}
