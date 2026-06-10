package se.laz.casual.info;

import java.util.Objects;

public class EventServiceStatistics
{
    String name;
    char order;
    long count;
    long total;
    long min;
    long max;
    long last;

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

    public void increment()
    {
        this.count++;
    }

    public void setMin( long value )
    {
        if( this.min == 0 || value < this.min )
        {
            this.min = value;
        }
    }

    public void setMax( long value )
    {
        if( this.max == 0 || value > this.max )
        {
            this.max = value;
        }
    }

    public void setLast( long value )
    {
        this.last = value;
    }


    public void increaseTotal( long value )
    {
        this.total += value;
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

        public EventServiceStatistics build()
        {
            return new EventServiceStatistics( this );
        }
    }
}
