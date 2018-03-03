/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer.fielded;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ForwardingInvocationHandler implements InvocationHandler
{
    private Object target;

    public ForwardingInvocationHandler(Object target )
    {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Class<?>[] paramClass = null;
        if( args != null )
        {
            paramClass = new Class<?>[args.length];
            for( int i=0; i< args.length; i++ )
            {
                paramClass[i] = args[i].getClass();
            }
        }
        return target.getClass().getMethod( method.getName(), paramClass ).invoke( target, args );
    }
}
