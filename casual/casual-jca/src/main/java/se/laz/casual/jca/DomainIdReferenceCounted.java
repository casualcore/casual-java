/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class DomainIdReferenceCounted
{
    private final DomainId domainId;
    private final AtomicInteger referenceCount = new AtomicInteger(0);

    private DomainIdReferenceCounted(DomainId domainId)
    {
        this.domainId = domainId;
    }

    public static DomainIdReferenceCounted of(DomainId domainId)
    {
        Objects.requireNonNull(domainId, "domainId can not be null");
        return new DomainIdReferenceCounted(domainId);
    }

    public DomainId getDomainId()
    {
        return domainId;
    }

    public int incrementAndGet()
    {
        return referenceCount.incrementAndGet();
    }

    public int decrementAndGet()
    {
        return referenceCount.decrementAndGet();
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
        DomainIdReferenceCounted that = (DomainIdReferenceCounted) o;
        return getDomainId().equals(that.getDomainId()) && referenceCount.get() == that.referenceCount.get();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getDomainId(), referenceCount);
    }

    @Override
    public String toString()
    {
        return "DomainIdReferenceCounted{" +
                "domainId=" + domainId +
                ", referenceCount=" + referenceCount +
                '}';
    }
}
