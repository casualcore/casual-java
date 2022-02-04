/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class SimpleArrayPojo implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(name = "FLD_LONG1")
    private final int[] numbers;
    @CasualFieldElement(name = "FLD_LONG4", lengthName = "FLD_LONG2")
    private final Long[] wrappedNumbers;
    // NOP-constructor needed
    private SimpleArrayPojo()
    {
        this.numbers = null;
        this.wrappedNumbers = null;
    }
    private SimpleArrayPojo(final int[] numbers, final Long[] wrappedNumbers)
    {
        this.numbers = numbers;
        this.wrappedNumbers = wrappedNumbers;
    }
    public static SimpleArrayPojo of(final int[] numbers, final Long[] wrappedNumbers)
    {
        Objects.requireNonNull(numbers);
        return new SimpleArrayPojo(Arrays.copyOf(numbers, numbers.length), Arrays.copyOf(wrappedNumbers, wrappedNumbers.length));
    }

    public int[] getNumbers()
    {
        return numbers;
    }

    public Long[] getWrappedNumbers()
    {
        return wrappedNumbers;
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
        SimpleArrayPojo that = (SimpleArrayPojo) o;
        return Arrays.equals(numbers, that.numbers) &&
            Arrays.equals(wrappedNumbers, that.wrappedNumbers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(numbers, wrappedNumbers);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SimpleArrayPojo{");
        sb.append("numbers=").append(Arrays.toString(numbers));
        sb.append(", wrappedNumbers=").append(Arrays.toString(wrappedNumbers));
        sb.append('}');
        return sb.toString();
    }
}
