package se.kodarkatten.casual.api.flags;

import se.kodarkatten.casual.api.flags.internal.CasualFlag;

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

    public T getFlagType()
    {
        return type;
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

    int getFlagValue() {
        return flags;
    }
}
