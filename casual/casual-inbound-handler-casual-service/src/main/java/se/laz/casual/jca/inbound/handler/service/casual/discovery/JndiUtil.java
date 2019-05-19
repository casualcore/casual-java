/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual.discovery;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class JndiUtil
{
    private static final String JAVA_GLOBAL_CONTEXT = "java:global/";
    private static final String BEAN_INTERFACE_SEPERATOR = "!";
    private static final String SEPERATOR = "/";
    private static final String PACKAGE_SEPERATOR = ".";
    private static final String CURRENT_CONTEXT = "";

    private JndiUtil()
    {}

    public static Map<String,Map<String,Proxy>> findAllGlobalJndiProxies( Context ctx ) throws NamingException
    {
        Map<String,Map<String,Proxy>> map = new HashMap<>();

        String path = JAVA_GLOBAL_CONTEXT;
        NamingEnumeration<NameClassPair> list = ctx.list( path );

        while( list.hasMoreElements() )
        {
            NameClassPair next = list.next();
            String name = next.getName();
            String jndiPath = path + name;

            map.put( jndiPath, findAllProxyInstance( (Context)ctx.lookup( jndiPath ), jndiPath ) );
        }
        return map;
    }

    private static Map<String,Proxy> findAllProxyInstance( Context ctx, String jndiPath ) throws NamingException
    {
        Map<String,Proxy> results = new HashMap<>();
        NamingEnumeration<NameClassPair> list = ctx.list(CURRENT_CONTEXT);
        while( list.hasMoreElements() )
        {
            NameClassPair next = list.next();
            String name = next.getName();
            String childPath = jndiPath + (jndiPath.contains(BEAN_INTERFACE_SEPERATOR) ? PACKAGE_SEPERATOR : SEPERATOR ) + name;

            Object tmp = ctx.lookup(name);
            if (tmp instanceof Context)
            {
                results.putAll( findAllProxyInstance((Context) tmp, childPath ) );
            }
            else if( tmp instanceof Proxy )
            {
                results.put(childPath, (Proxy)tmp);
            }
        }
        return results;
    }
}
