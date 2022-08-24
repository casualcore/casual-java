package se.laz.casual.connection.caller.entities;

import java.util.Objects;

public class CacheEntryWithHops
{
    private final CacheEntry cacheEntry;
    private long hops;
    private CacheEntryWithHops(CacheEntry cacheEntry, long hops)
    {
        this.cacheEntry = cacheEntry;
        this.hops = hops;
    }

    public static CacheEntryWithHops of(CacheEntry cacheEntry, long hops)
    {
        Objects.requireNonNull(cacheEntry, "cacheEntry can not be null");
        return new CacheEntryWithHops(cacheEntry, hops);
    }

    public CacheEntry getCacheEntry()
    {
        return cacheEntry;
    }

    public long getHops()
    {
        return hops;
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
        CacheEntryWithHops that = (CacheEntryWithHops) o;
        return getHops() == that.getHops() && getCacheEntry().equals(that.getCacheEntry());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getCacheEntry(), getHops());
    }

    @Override
    public String toString()
    {
        return "CacheEntryWithHops{" +
                "cacheEntry=" + cacheEntry +
                ", hops=" + hops +
                '}';
    }
}
