package se.laz.casual.connection.caller.entities;

import java.util.Comparator;

public class CacheEntryWithHopsComparator implements Comparator<CacheEntryWithHops>
{
    private CacheEntryWithHopsComparator()
    {}
    @Override
    public int compare(CacheEntryWithHops first, CacheEntryWithHops second)
    {
        int val = Math.toIntExact(first.getHops() - second.getHops());
        if(val == 0)
        {
            return 0;
        }
        return val > 0 ? 1 : -1;
    }
    public static Comparator<CacheEntryWithHops> of()
    {
        return new CacheEntryWithHopsComparator();
    }
}