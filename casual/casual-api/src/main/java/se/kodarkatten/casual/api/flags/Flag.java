package se.kodarkatten.casual.api.flags;

import se.kodarkatten.casual.api.flags.internal.CasualFlag;

/**
 * @author jone
 */
public class Flag
{
    private final int flags;

    Flag(int flags)
    {
        this.flags = flags;
    }


    public static final class Builder
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

        public final Builder and(final CasualFlag flag)
        {
            value = value & flag.getValue();
            return this;
        }

        public final Builder or(final CasualFlag flag)
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
