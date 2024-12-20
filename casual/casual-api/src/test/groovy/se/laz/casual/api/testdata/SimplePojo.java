/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
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
    private String name;
    @CasualFieldElement(name = "FLD_LONG1")
    private Integer age;
    // NOP-constructor needed
    private SimplePojo()
    {}
    private SimplePojo(final String name, Integer age)
    {
        this.name = name;
        this.age = age;
    }

    public static SimplePojo of(final String name, Integer age)
    {
        return new SimplePojo(name, age);
    }

    public String getName()
    {
        return name;
    }

    public Integer getAge()
    {
        return age;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof SimplePojo that))
        {
            return false;
        }
        return Objects.equals(getName(), that.getName()) && Objects.equals(getAge(), that.getAge());
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
