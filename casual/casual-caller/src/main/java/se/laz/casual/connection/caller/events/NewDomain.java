/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller.events;

import se.laz.casual.connection.caller.entities.Pool;

import java.util.Objects;

public class NewDomain
{
    private final Pool pool;

    private NewDomain(Pool pool)
    {
        this.pool = pool;
    }

    public static NewDomain of(Pool pool)
    {
        Objects.requireNonNull(pool, "pool can not be null");
        return new NewDomain(pool);
    }

    public Pool getPool()
    {
        return pool;
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
        NewDomain that = (NewDomain) o;
        return pool.equals(that.pool);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pool);
    }

    @Override
    public String toString()
    {
        return "NewDomainEvent{" +
                "pool=" + pool +
                '}';
    }
}

