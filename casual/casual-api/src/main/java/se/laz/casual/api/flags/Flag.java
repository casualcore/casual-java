/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.flags;

import se.laz.casual.api.flags.internal.CasualFlag;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author jone
 */
public class Flag<T extends CasualFlag> implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int flags;

    private Flag(int flags)
    {
        this.flags = flags;
    }

    public static <T extends CasualFlag> Flag<T> of(final Flag<T> flag)
    {
        return new Flag<>(flag.flags);
    }

    public static <T extends CasualFlag> Flag<T> of(final T type)
    {
        return new Flag<>(type.getValue());
    }

    public static <T extends CasualFlag> Flag<T> of()
    {
        return new Flag<>(0);
    }

    public int getFlagValue()
    {
        return flags;
    }

    public Flag<T> setFlag(final T v)
    {
        flags |= v.getValue();
        return this;
    }

    public Flag<T> clearFlag(final T v)
    {
        flags &= ~(v.getValue());
        return this;
    }

    public boolean isSet(final T v)
    {
        return (0 != (flags & (v.getValue())));
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

        public final Builder<T> and(final T flag)
        {
            value = value & flag.getValue();
            return this;
        }

        public final Builder<T> or(final T flag)
        {
            value = value | flag.getValue();
            return this;
        }

        public <X extends CasualFlag> Flag<X> build()
        {
            return new Flag<>(value);
        }
    }
}
