/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class WrappedListPojo implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement
    private final List<SimplePojo> simplePojos;
    private WrappedListPojo(final List<SimplePojo> simplePojos)
    {
        this.simplePojos = simplePojos;
    }
    // NOP-constructor needed
    private WrappedListPojo()
    {
        simplePojos = null;
    }
    public static WrappedListPojo of(final List<SimplePojo> simplePojos)
    {
        Objects.requireNonNull(simplePojos);
        return new WrappedListPojo(simplePojos.stream().collect(Collectors.toList()));
    }
    public List<SimplePojo> getSimplePojos()
    {
        return simplePojos;
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
        WrappedListPojo that = (WrappedListPojo) o;
        return Objects.equals(simplePojos, that.simplePojos);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(simplePojos);
    }
}
