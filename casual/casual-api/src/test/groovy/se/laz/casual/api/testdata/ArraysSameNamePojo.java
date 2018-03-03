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

public final class ArraysSameNamePojo implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(name = "FLD_LONG1", lengthName = "FLD_LONG3")
    private final int[] cats;
    @CasualFieldElement(name = "FLD_LONG1", lengthName = "FLD_LONG4")
    private final int[] dogs;

    private ArraysSameNamePojo(final int[] cats, final int[] dogs)
    {
        this.cats = cats;
        this.dogs = dogs;
    }

    public static ArraysSameNamePojo of(final int[] cats, final int[] dogs)
    {
        Objects.requireNonNull(cats);
        Objects.requireNonNull(dogs);
        return new ArraysSameNamePojo(cats, dogs);
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
        ArraysSameNamePojo that = (ArraysSameNamePojo) o;
        return Arrays.equals(cats, that.cats) &&
            Arrays.equals(dogs, that.dogs);
    }

    @Override
    public int hashCode()
    {

        int result = Arrays.hashCode(cats);
        result = 31 * result + Arrays.hashCode(dogs);
        return result;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("ArraysSameNamePojo{");
        sb.append("cats=").append(Arrays.toString(cats));
        sb.append(", dogs=").append(Arrays.toString(dogs));
        sb.append('}');
        return sb.toString();
    }
}
