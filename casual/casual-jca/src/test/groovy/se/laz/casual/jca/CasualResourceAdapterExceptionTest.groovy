/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Shared
import spock.lang.Specification

class CasualResourceAdapterExceptionTest extends Specification
{
    @Shared CasualResourceAdapterException instance
    @Shared String message
    @Shared Throwable throwable

    def setup()
    {
        message = "error message."
        throwable = new RuntimeException( "runtime error" )
    }

    def "Constructor check."()
    {
        setup:
        instance = new CasualResourceAdapterException( )

        expect:
        instance.getMessage() == null
        instance.getCause() == null
    }

    def "Constructor message check."()
    {
        setup:
        instance = new CasualResourceAdapterException( message )

        expect:
        instance.getMessage() == message
        instance.getCause() == null
    }

    def "Constructor throwable check."()
    {
        setup:
        instance = new CasualResourceAdapterException( throwable )

        expect:
        instance.getMessage() == throwable.toString()
        instance.getCause() == throwable
    }

    def "Constructor message and throwable check."()
    {
        setup:
        instance = new CasualResourceAdapterException( message, throwable )

        expect:
        instance.getMessage() == message
        instance.getCause() == throwable
    }
}
