package se.laz.casual.connection.caller;

import java.util.Objects;

public class NewDomainEvent
{
    private final Pool pool;

    private NewDomainEvent(Pool pool)
    {
        this.pool = pool;
    }

    public static NewDomainEvent of(Pool pool)
    {
        Objects.requireNonNull(pool, "pool can not be null");
        return new NewDomainEvent(pool);
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
        NewDomainEvent that = (NewDomainEvent) o;
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

