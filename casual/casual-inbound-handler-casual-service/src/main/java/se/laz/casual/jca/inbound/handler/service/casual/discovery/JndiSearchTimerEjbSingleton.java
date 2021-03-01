/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual.discovery;

import se.laz.casual.api.service.CasualService;
import se.laz.casual.jca.inbound.handler.HandlerException;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceEntry;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceMetaData;
import se.laz.casual.jca.inbound.handler.service.casual.CasualServiceRegistry;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static se.laz.casual.jca.inbound.handler.service.casual.discovery.MethodMatcher.matches;

/**
 * Periodically check to find any newly deployed {@link CasualService} annotated EJBs.
 *
 * https://stackoverflow.com/questions/48867612/determine-jndi-portable-name-within-an-javax-enterprise-inject-spi-extension/50195996#50195996
 */
@Singleton
public class JndiSearchTimerEjbSingleton
{
    private static final Logger logger = Logger.getLogger(JndiSearchTimerEjbSingleton.class.getName());

    @Schedule(hour = "*", minute = "*", second = "*/10", persistent = false)
    public void findServicesInJndi()
    {
        try
        {
            logger.finest( ()-> "Fetch all unresolved casual services." );
            List<CasualServiceMetaData> toFind = CasualServiceRegistry.getInstance().getUnresolvedServices();
            logger.finest( ()-> "Unresolved: " + toFind.size() );
            if( toFind.isEmpty() )
            {
                return;
            }
            logger.finest( ()-> "Fetch all global apps." );
            Map<String,Map<String,Proxy>> apps = JndiUtil.findAllGlobalJndiProxies( new InitialContext() );

            resolveAll( toFind, apps );

        } catch (NamingException e)
        {
            logger.log( Level.WARNING, e, ()-> "Error with jndi lookup." );
        }
    }

    private void resolveAll(List<CasualServiceMetaData> toFind, Map<String,Map<String,Proxy>> apps )
    {
        toFind.forEach( c -> resolve( c, apps ) );
    }

    private void resolve(CasualServiceMetaData entry, Map<String,Map<String,Proxy>> apps )
    {
        for( Map<String, Proxy> app: apps.values() )
        {
            CasualServiceEntry found = searchInApp( entry, app );
            if( found != null )
            {
                CasualServiceRegistry.getInstance().register( found );
                break;
            }
        }
    }

    private CasualServiceEntry searchInApp(CasualServiceMetaData entry, Map<String,Proxy> app )
    {
        if( app.isEmpty() )
        {
            return null;
        }

        Proxy p = app.values().stream().findFirst().orElseThrow( ()-> new HandlerException("Proxy map is not populated correctly." ) );
        String name = entry.getImplementationClass().getName();
        try
        {
            Class<?> found = p.getClass().getClassLoader().loadClass( name );
            Method method = entry.getServiceMethod();

            if( ! hasMatchingAnnotation( entry, found, method ) )
            {
                return null;
            }

            String jndi = findJndiName( entry, new ArrayList<>(app.keySet()) );

            Method proxyMethod = findMatchingProxyMethod( app.get( jndi ), method );

            return jndi != null ? CasualServiceEntry.of( entry.getServiceName(), jndi, proxyMethod ) : null;
        }
        catch( ClassNotFoundException | NoSuchMethodException e)
        {
            return null;
        }
    }

    private boolean hasMatchingAnnotation(CasualServiceMetaData metaData, Class<?> found, Method method ) throws ClassNotFoundException, NoSuchMethodException
    {
        Method foundMethod = Arrays.stream(found.getDeclaredMethods())
                .filter(m -> matches(m, method))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("Unable to find method in found class matching: " + method));

        CasualService s = foundMethod.getAnnotation(CasualService.class);

        return metaData.getServiceName().equals(s.name());
    }

    private Method findMatchingProxyMethod(Proxy proxy, Method method )
    {
        if( proxy == null )
        {
            return null;
        }
        return Arrays.stream(proxy.getClass().getDeclaredMethods())
                    .filter(m -> matches(m, method))
                    .findFirst()
                    .orElse(null );
    }

    private String findJndiName( CasualServiceMetaData entry, List<String> jndiNames )
    {
        String implClass = entry.getImplementationClass().getName();
        String ejbName = entry.getEjbName().orElse( null );
        String interfaceClass = entry.getInterfaceClass().map(Class::getName).orElse( null );

        return JndiMatcher.findMatch( implClass, ejbName, interfaceClass, jndiNames );
    }
}
