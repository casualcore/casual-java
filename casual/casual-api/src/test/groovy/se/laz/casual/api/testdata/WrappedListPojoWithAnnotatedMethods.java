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

public final class WrappedListPojoWithAnnotatedMethods implements Serializable
{
    private static final long serialVersionUID = 1;
    private List<SimplePojo> simplePojos;
    private WrappedListPojoWithAnnotatedMethods(final List<SimplePojo> simplePojos)
    {
        this.simplePojos = simplePojos;
    }
    // NOP-constructor needed
    private WrappedListPojoWithAnnotatedMethods()
    {}
    public static WrappedListPojoWithAnnotatedMethods of(final List<SimplePojo> simplePojos)
    {
        Objects.requireNonNull(simplePojos);
        return new WrappedListPojoWithAnnotatedMethods(simplePojos.stream().collect(Collectors.toList()));
    }
    @CasualFieldElement
    public List<SimplePojo> getSimplePojos()
    {
        return simplePojos;
    }

    public void setSimplePojos(@CasualFieldElement List<SimplePojo> simplePojos)
    {
        this.simplePojos = simplePojos;
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
        WrappedListPojoWithAnnotatedMethods that = (WrappedListPojoWithAnnotatedMethods) o;
        return Objects.equals(simplePojos, that.simplePojos);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(simplePojos);
    }
}
