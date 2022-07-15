/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class DomainId implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private DomainId(UUID id)
    {
        this.id = id;
    }

    public static DomainId of(UUID id)
    {
        Objects.requireNonNull(id, "id can not be null");
        return new DomainId(id);
    }

    public UUID getId()
    {
        return id;
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
        DomainId domainId = (DomainId) o;
        return Objects.equals(getId(), domainId.getId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getId());
    }

    @Override
    public String toString()
    {
        return "DomainId{" +
                "id=" + id +
                '}';
    }
}
