/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.Objects;

public final class SimplePojo implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(name = "FLD_STRING2")
    private final String name;
    @CasualFieldElement(name = "FLD_LONG1")
    private final int age;
    // NOP-constructor needed
    private SimplePojo()
    {
        name = null;
        age = 0;
    }
    private SimplePojo(final String name, int age)
    {
        this.name = name;
        this.age = age;
    }

    public static SimplePojo of(final String name, int age)
    {
        Objects.requireNonNull(name);
        return new SimplePojo(name, age);
    }

    public String getName()
    {
        return name;
    }

    public int getAge()
    {
        return age;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplePojo that = (SimplePojo) o;
        return age == that.age &&
            Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, age);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SimplePojo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", age=").append(age);
        sb.append('}');
        return sb.toString();
    }
}
