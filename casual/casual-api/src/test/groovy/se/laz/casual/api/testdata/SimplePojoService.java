/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import java.util.Objects;

public final class SimplePojoService
{
    private SimplePojo pojo;
    private SimplePojoService(SimplePojo pojo)
    {
        this.pojo = pojo;
    }
    // NOP-constructor needed
    private SimplePojoService()
    {}
    public static SimplePojoService of(SimplePojo pojo)
    {
        Objects.requireNonNull(pojo);
        return new SimplePojoService(pojo);
    }

    public static SimplePojoService of()
    {
        return new SimplePojoService(null);
    }

    public void setPojo(SimplePojo pojo)
    {
        this.pojo = pojo;
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
        SimplePojoService that = (SimplePojoService) o;
        return Objects.equals(pojo, that.pojo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pojo);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SimplePojoService{");
        sb.append("pojo=").append(pojo);
        sb.append('}');
        return sb.toString();
    }
}
