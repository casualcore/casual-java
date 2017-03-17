package se.kodarkatten.casual.api.flags;

import se.kodarkatten.casual.api.flags.internal.CasualFlag;

import java.util.Objects;

/**
 * @author jone
 */
public class Flag<T extends CasualFlag>
{
    private T type;
    private final int flags;

    private Flag(int flags)
    {
        this.flags = flags;
    }

    public static Flag of(final Flag flag)
    {
        return new Flag(flag.flags);
    }

    public static <T extends CasualFlag> Flag<T> of(final T type)
    {
        return new Flag(type.getValue());
    }

    public T getFlagType()
    {
        return type;
    }

    public int getFlagValue()
    {
        return flags;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass()) return false;
        Flag<?> flag = (Flag<?>) o;
        return flags == flag.flags;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(flags);
    }

    public static final class Builder<T extends CasualFlag>
    {
        private int value;

        public Builder()
        {
            value = 0;
        }

        public Builder(final int initialValue)
        {
            value = initialValue;
        }

        public final Builder and(final T flag)
        {
            value = value & flag.getValue();
            return this;
        }

        public final Builder or(final T flag)
        {
            value = value | flag.getValue();
            return this;
        }

        public Flag build()
        {
            return new Flag(value);
        }
    }
}
