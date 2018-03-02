package se.kodarkatten.casual.jca.inbound.handler.service.casual.discovery;

import java.lang.reflect.Method;

/**
 * Matches method which are the same even if they are loaded from different classloaders.
 * Used to help fix issue ith classloaders in weblogic.
 */
public final class MethodMatcher
{
    private MethodMatcher()
    {

    }
    public static boolean matches( Method m1, Method m2 )
    {
        if( areAlreadyEqual( m1, m2 ) )
        {
            return true;
        }
        return namesMatch( m1, m2 ) &&
                parameterCountsMatch( m1, m2 ) &&
                methodParamsMatch( m1, m2 );
    }

    private static boolean areAlreadyEqual(Method m1, Method m2 )
    {
        return m1.equals( m2 );
    }

    private static boolean namesMatch( Method m1, Method m2 )
    {
        return m1.getName().equals( m2.getName() );
    }

    private static boolean parameterCountsMatch( Method m1, Method m2 )
    {
        return m1.getParameterCount() == m2.getParameterCount();
    }

    private static boolean methodParamsMatch( Method m1, Method m2 )
    {
        Class<?>[] params1 = m1.getParameterTypes();
        Class<?>[] params2 = m2.getParameterTypes();
        for( int i=0;i<params1.length;i++)
        {
            String p1 = params1[i].getName();
            String p2 = params2[i].getName();
            if( !p1.equals( p2 ) )
            {
                return false;
            }
        }
        return true;
    }
}
