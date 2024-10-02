/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SimpleListPojo implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(name = "FLD_STRING1", lengthName = "FLD_LONG1")
    final List<String> strings;

    @CasualFieldElement(name = "FLD_LONG3", lengthName = "FLD_LONG2")
    final List<Integer> numbers;
    // NOP-constructor needed
    private SimpleListPojo()
    {
        strings = null;
        numbers = null;
    }
    private SimpleListPojo(final List<String> strings, final List<Integer> numbers)
    {
        this.strings = strings;
        this.numbers = numbers;
    }
    public static SimpleListPojo of(final List<String> l, final List<Integer> numbers)
    {
        Objects.requireNonNull(l);
        return new SimpleListPojo(new ArrayList<>(l), new ArrayList<>(numbers));
    }

    public List<String> getStrings()
    {
        return strings.stream().toList();
    }

    public List<Integer> getNumbers()
    {
        return numbers.stream().toList();
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
        SimpleListPojo that = (SimpleListPojo) o;
        return Objects.equals(strings, that.strings) &&
            Objects.equals(numbers, that.numbers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(strings, numbers);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SimpleListPojo{");
        sb.append("strings=").append(strings);
        sb.append(", numbers=").append(numbers);
        sb.append('}');
        return sb.toString();
    }
}
