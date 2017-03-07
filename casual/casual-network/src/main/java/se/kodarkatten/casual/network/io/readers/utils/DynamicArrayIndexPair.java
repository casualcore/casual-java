package se.kodarkatten.casual.network.io.readers.utils;

import java.util.List;

/**
 * Created by aleph on 2017-03-07.
 */
public class DynamicArrayIndexPair<T>
{
    private List<T> items;
    private int index;
    // Apparently sonar can not see the usage
    @SuppressWarnings("squid:UnusedPrivateMethod")
    private DynamicArrayIndexPair(List<T> items, int index)
    {
        this.items = items;
        this.index = index;
    }
    public static <T> DynamicArrayIndexPair of(List<T> names, int index)
    {
        return new DynamicArrayIndexPair(names, index);
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
