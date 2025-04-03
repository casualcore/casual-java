/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.k8s.exec

import spock.lang.Specification

class ExecResultTest extends Specification
{
    int exitCode = 0
    String output = "hello there"

    ExecResult instance

    def setup()
    {
        instance = ExecResult.newBuilder.exitCode( exitCode ).output( output ).build()
    }

    def "Create then retrieve."()
    {
        expect:
        instance.getExitCode() == exitCode
        instance.getOutput() == output
    }

    def "equals and hashcode"()
    {
        when:
        ExecResult instance2 = ExecResult.newBuilder( instance ).build()
        ExecResult instance3 = ExecResult.newBuilder( instance ).exitCode( 1 ).build()

        then:
        instance == instance
        instance == instance2
        instance != instance3
        instance.hashCode(  ) == instance.hashCode(  )
        instance.hashCode(  ) == instance2.hashCode(  )
        instance3.hashCode(  ) !=  instance.hashCode(  )
        !instance.equals( "string" )
    }

    def "Check toString"()
    {
        when:
        String actual = instance.toString(  )

        then:
        actual != null
        actual.contains( ""+exitCode )
        actual.contains( output )
    }

}
