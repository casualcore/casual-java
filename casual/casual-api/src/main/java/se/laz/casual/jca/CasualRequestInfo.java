/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import jakarta.resource.spi.ConnectionRequestInfo;
import java.util.Objects;
import java.util.Optional;

public class CasualRequestInfo implements ConnectionRequestInfo
{
    private final DomainId domainId;
    private CasualRequestInfo(DomainId domainId)
    {
        this.domainId = domainId;
    }

    public static ConnectionRequestInfo of(DomainId domainId)
    {
        Objects.requireNonNull(domainId, "domainId can not be null");
        return new CasualRequestInfo(domainId);
    }

    public Optional<DomainId> getDomainId()
    {
        return Optional.ofNullable(domainId);
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
        CasualRequestInfo that = (CasualRequestInfo) o;
        return getDomainId().equals(that.getDomainId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getDomainId());
    }

    @Override
    public String toString()
    {
        return "CasualRequestInfo{" +
                "domainId=" + domainId +
                '}';
    }
}
