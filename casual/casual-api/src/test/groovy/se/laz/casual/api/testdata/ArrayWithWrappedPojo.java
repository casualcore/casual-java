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

public final class ArrayWithWrappedPojo implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(lengthName = "FLD_LONG3")
    private final SimplePojo[] simplePojos;
    private ArrayWithWrappedPojo(SimplePojo[] simplePojos)
    {
        this.simplePojos = simplePojos;
    }
    public static ArrayWithWrappedPojo of(final SimplePojo[] simplePojos)
    {
        Objects.requireNonNull(simplePojos);
        return new ArrayWithWrappedPojo(simplePojos);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayWithWrappedPojo that = (ArrayWithWrappedPojo) o;
        return Arrays.equals(simplePojos, that.simplePojos);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(simplePojos);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("ArrayWithWrappedPojo{");
        sb.append("simplePojos=").append(Arrays.toString(simplePojos));
        sb.append('}');
        return sb.toString();
    }
}
