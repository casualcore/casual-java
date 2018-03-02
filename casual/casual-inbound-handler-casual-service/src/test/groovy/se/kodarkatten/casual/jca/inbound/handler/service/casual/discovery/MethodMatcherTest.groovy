package se.kodarkatten.casual.jca.inbound.handler.service.casual.discovery

import se.kodarkatten.casual.jca.inbound.handler.service.casual.SimpleObject
import se.kodarkatten.casual.jca.inbound.handler.service.casual.SimpleService
import se.kodarkatten.casual.jca.inbound.handler.service.casual.TempTestJarTool
import se.kodarkatten.casual.jca.inbound.handler.service.casual.TestClassLoaderTool
import se.kodarkatten.casual.jca.inbound.handler.service.casual.discovery.MethodMatcher
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class MethodMatcherTest extends Specification
{
    @Shared File jarFile = TempTestJarTool.create( SimpleObject.class, SimpleService.class )
    @Shared ClassLoader classloader1
    @Shared ClassLoader classloader2
    @Shared Class<?> instance1
    @Shared Class<?> instance2

    def setup()
    {
        classloader1 = TestClassLoaderTool.createClassLoader( jarFile )
        instance1 = classloader1.loadClass( SimpleObject.getName())
        classloader2 = TestClassLoaderTool.createClassLoader( jarFile )
        instance2 = classloader2.loadClass( SimpleObject.getName())
    }

    def "Matches"()
    {
        setup:
        Method m1 = instance1.getMethod( methodName1, classes1 )
        Method m2 = instance2.getMethod( methodName2, classes2 )

        expect:
        m1 != m2
        MethodMatcher.matches( m1, m2 )
        MethodMatcher.matches( m1, m1 )
        MethodMatcher.matches( m2, m2 )

        where:
        methodName1 | methodName2 | classes1     | classes2
        "hashCode"  | "hashCode"  | null         | null
        "equals"    | "equals"    | Object.class | Object.class
        "getMessage"| "getMessage"| String.class | String.class
        "getMessage"| "getMessage"| Integer.class | Integer.class
    }

    def "Does not match"()
    {
        setup:
        Method m1 = instance1.getMethod( methodName1, classes1 )
        Method m2 = instance2.getMethod( methodName2, classes2 )

        expect:
        m1 != m2
        ! MethodMatcher.matches( m1, m2 )
        MethodMatcher.matches( m1, m1 )
        MethodMatcher.matches( m2, m2 )

        where:
        methodName1 | methodName2 | classes1     | classes2
        "hashCode"  | "equals"  | null         | Object.class
        "getMessage"| "getMessage"| String.class | Integer.class
    }
}
