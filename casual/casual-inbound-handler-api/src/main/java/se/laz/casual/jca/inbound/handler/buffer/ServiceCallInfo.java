/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Information for the dispatch of the call to the service.
 * This includes the method parameters and potentially the method if the service maps only to a class.
 */
public class ServiceCallInfo
{

    private Method method;
    private Method realMethod;
    private Object[] params;

    private ServiceCallInfo(Method method, Method realMethod, Object[] params )
    {
        this.method = method;
        this.params = params;
        this.realMethod = realMethod;
    }

    public static ServiceCallInfo of(Object[] params )
    {
        return of( null, null, params );
    }

    public static ServiceCallInfo of(Method method, Object[] params)
    {
        return of( method, null, params );
    }

    public static ServiceCallInfo of(Method method, Method realMethod, Object[] params)
    {
        return new ServiceCallInfo( method, realMethod, params );
    }

    public Optional<Method> getMethod()
    {
        return Optional.ofNullable( method );
    }

    public Optional<Method> getRealMethod()
    {
        return Optional.ofNullable( realMethod );
    }

    public Object[] getParams()
    {
        return this.params;
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
        ServiceCallInfo that = (ServiceCallInfo) o;
        return Objects.equals(method, that.method) &&
                Objects.equals(realMethod, that.realMethod) &&
                Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(method, realMethod, params);
    }

    @Override
    public String toString()
    {
        return "ServiceCallInfo{" +
                "method=" + method +
                "realMethod=" + realMethod +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
