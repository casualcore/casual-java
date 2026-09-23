package se.laz.casual.info;

import java.util.Objects;

/**
 * Statistics for a service
 */
public class EventServiceStatistics
{
    private final String name;
    private final char order;
    private final long count;
    private final long total;
    private final long min;
    private final long max;
    private final long last;

    public EventServiceStatistics( Builder builder )
    {
        this.name = builder.name;
        this.order = builder.order;
        this.count = builder.count;
        this.total = builder.total;
        this.min = builder.min;
        this.max = builder.max;
        this.last = builder.last;
    }

    public String getName()
    {
        return name;
    }

    public char getOrder()
    {
        return order;
    }

    public long getCount()
    {
        return count;
    }

    public long getTotal()
    {
        return total;
    }

    public long getMin()
    {
        return min;
    }

    public long getMax()
    {
        return max;
    }

    public long getLast()
    {
        return last;
    }

    public static Builder newBuilder( EventServiceStatistics eventServiceStatistics )
    {
        return new Builder()
                .name( eventServiceStatistics.name )
                .order( eventServiceStatistics.order )
                .count( eventServiceStatistics.count )
                .total( eventServiceStatistics.total )
                .min( eventServiceStatistics.min )
                .max( eventServiceStatistics.max )
                .last( eventServiceStatistics.last );
    }


    @Override
    public boolean equals( Object o )
    {
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        EventServiceStatistics that = (EventServiceStatistics) o;
        return count == that.count && total == that.total && min == that.min && max == that.max && last == that.last && Objects.equals( name, that.name ) && order == that.order;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( name, order, count, total, min, max, last );
    }

    @Override
    public String toString()
    {
        return "EventServiceStatistics{" +
                "name='" + name + '\'' +
                ", order=" + order +
                ", count=" + count +
                ", total=" + total +
                ", min=" + min +
                ", max=" + max +
                ", last=" + last +
                '}';
    }

    public static class Builder
    {
        private String name;
        private char order;
        private long count = 0;
        private long total = 0;
        private long min = 0;
        private long max = 0;
        private long last = 0;

        public EventServiceStatistics.Builder name( String name )
        {
            this.name = name;
            return this;
        }

        public EventServiceStatistics.Builder order( char order )
        {
            this.order = order;
            return this;
        }

        public EventServiceStatistics.Builder count( long count )
        {
            this.count = count;
            return this;
        }

        public EventServiceStatistics.Builder total( long total )
        {
            this.total = total;
            return this;
        }

        public EventServiceStatistics.Builder min( long min )
        {
            if( this.min == 0 || this.min > min )
            {
                this.min = min;
            }
            return this;
        }

        public EventServiceStatistics.Builder max( long max )
        {
            if( this.max == 0 || this.max < max )
            {
                this.max = max;
            }
            return this;
        }

        public EventServiceStatistics.Builder last( long last )
        {
            this.last = last;
            return this;
        }

        public void increment()
        {
            this.count++;
        }

        public void increaseTotal( long value )
        {
            this.total += value;
        }

        public EventServiceStatistics build()
        {
            return new EventServiceStatistics( this );
        }
    }
}
