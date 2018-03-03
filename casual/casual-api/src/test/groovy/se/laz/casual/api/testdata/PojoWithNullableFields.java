/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.Objects;

public final class PojoWithNullableFields implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(name = "FLD_STRING1")
    private String name;
    @CasualFieldElement(name = "FLD_LONG1")
    private Integer age;

    private PojoWithNullableFields(String name, Integer age)
    {
        this.name = name;
        this.age = age;
    }

    public static PojoWithNullableFields of(String name, Integer age)
    {
        return new PojoWithNullableFields(name, age);
    }

    public String getName()
    {
        return name;
    }

    public PojoWithNullableFields setName(String name)
    {
        this.name = name;
        return this;
    }

    public Integer getAge()
    {
        return age;
    }

    public PojoWithNullableFields setAge(Integer age)
    {
        this.age = age;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PojoWithNullableFields that = (PojoWithNullableFields) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(age, that.age);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(name, age);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("PojoWithNullableFields{");
        sb.append("name='").append(name).append('\'');
        sb.append(", age=").append(age);
        sb.append('}');
        return sb.toString();
    }
}
