/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
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
    private Object[] params;

    private ServiceCallInfo(Method method, Object[] params )
    {
        this.method = method;
        this.params = params;
    }

    public static ServiceCallInfo of(Object[] params )
    {
        return of( null, params );
    }

    public static ServiceCallInfo of(Method method, Object[] params)
    {
        return new ServiceCallInfo( method, params );
    }

    public Optional<Method> getMethod()
    {
        return Optional.ofNullable( method );
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
                Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(method, params);
    }

    @Override
    public String toString()
    {
        return "ServiceCallInfo{" +
                "method=" + method +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
