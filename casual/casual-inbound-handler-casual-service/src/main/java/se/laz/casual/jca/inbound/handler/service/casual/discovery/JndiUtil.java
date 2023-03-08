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
import java.util.logging.Level;
import java.util.logging.Logger;

public class JndiUtil
{
    private static final Logger logger = Logger.getLogger(JndiUtil.class.getName());
    private static final String BEAN_INTERFACE_SEPERATOR = "!";
    private static final String SEPERATOR = "/";
    private static final String PACKAGE_SEPERATOR = ".";
    public static final String JAVA_GLOBAL_CONTEXT = "java:global/";
    public static final String CURRENT_CONTEXT = "";

    private JndiUtil()
    {}

    /**
     * Retrieve all the Global JNDI proxies from the provided {@link Context} - depth first search.
     * <p>
     * https://stackoverflow.com/questions/48867612/determine-jndi-portable-name-within-an-javax-enterprise-inject-spi-extension/50195996#50195996
     * </p>
     *
     * Example result for 2 deployed applications with 2 global proxies each would appear as follows:
     *
     * <pre>
     * java:global/casual-test-app-custom-2
     *      - java:global/casual-test-app-custom-2/test-ejb2/SimpleServiceNoViewEjb!se.laz.casual.example.serviceSimpleServiceNoViewEjb
     *          - ProxyObj1
     *      - java:global/casual-test-app-custom-2/test-ejb2/SimpleService2!se.laz.casual.example.service.ISimpleService2
     *          - ProxyObj2
     * java:global/casual-java-testapp
     *      - java:global/casual-java-testapp/CasualOrderService!se.laz.casual.example.service.order.ICasualOrderServiceRemote
     *          - ProxyObj3
     *      - java:global/casual-java-testapp/CasualOrderService!se.laz.casual.example.service.order.ICasualOrderService
     *          - ProxyObj4
     * </pre>
     *
     * <p>The first "applications" map has a key for each application mapping to a map of the application's contained proxies.</p>
     *
     * <p>The second "proxies" map has a key for each proxy's fully qualified JNDI name mapping to the proxy object itself.</p>
     *
     * <p>If there are no global proxies in the application, the second map will be an empty map.</p>
     *
     * @param ctx to search
     * @return Map of each deployed applications global proxies found in JNDI (See example above).
     * @throws NamingException if an error occurs iterating over the Java Global Context.
     */
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

            try
            {
                Object tmp = ctx.lookup(name);
                if (tmp instanceof Context)
                {
                    results.putAll(findAllProxyInstance((Context) tmp, childPath));
                }
                else if (tmp instanceof Proxy)
                {
                    results.put(childPath, (Proxy) tmp);
                }
            }
            catch(NamingException e)
            {
                logger.log( Level.FINEST, e, () -> "lookup failed for: " + name);
            }
        }
        return results;
    }
}
