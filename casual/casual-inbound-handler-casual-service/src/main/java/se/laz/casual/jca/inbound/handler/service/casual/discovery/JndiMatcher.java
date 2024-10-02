/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual.discovery;

import java.util.List;


/**
 * Facilitate the traversal of JNDI tree to find EJBs matching previously
 * discovered {@link se.laz.casual.api.service.CasualService} annotated methods.
 *
 * https://stackoverflow.com/questions/48867612/determine-jndi-portable-name-within-an-javax-enterprise-inject-spi-extension/50195996#50195996
 */
public class JndiMatcher
{
    private JndiMatcher()
    {}
    public static String findMatch(String implementationType, String ejbName, String interfaceType, List<String> jndiUrls )
    {
        List<String> found = jndiUrls.stream().filter( jndiUrl -> matches( implementationType, ejbName, interfaceType, jndiUrl ) ).toList();

        return found.size() == 1? found.get( 0 ) : null;
    }

    private static boolean matches( String implementationType, String ejbName, String interfaceType, String jndiUrl )
    {
        return matchesInterfaceNameAndEjbName( implementationType, ejbName, interfaceType, jndiUrl ) ||
                matchesImpelemntationInterfaceAndEjbName( implementationType, jndiUrl );
    }

    private static boolean matchesInterfaceNameAndEjbName( String implementationType, String ejbName, String interfaceType, String jndiUrl )
    {
        return endsWithInterfaceName( interfaceType, jndiUrl ) &&
                (
                        hasEjbName( ejbName, jndiUrl ) ||
                        hasEjbName( getTypeName( implementationType ), jndiUrl )
                );
    }

    private static boolean matchesImpelemntationInterfaceAndEjbName( String implementationType, String jndiUrl )
    {
        return endsWithInterfaceName( implementationType, jndiUrl ) && hasEjbName( getTypeName( implementationType ), jndiUrl );
    }

    private static boolean endsWithInterfaceName( String interfaceType, String jndiUrl )
    {
        if( interfaceType == null )
        {
            return false;
        }
        return jndiUrl.endsWith( interfaceType );
    }

    private static boolean hasEjbName( String ejbName, String jndiUrl )
    {
        if( ejbName == null )
        {
            return false;
        }
        return jndiUrl.contains( "/" + ejbName + "!" );
    }

    private static String getTypeName( String fullyQualifiedName )
    {
        String[] s = fullyQualifiedName.split( "\\." );
        return s[s.length -1];
    }
}
