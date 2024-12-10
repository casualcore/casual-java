/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test;

import java.util.concurrent.atomic.AtomicInteger;

public class StaticData
{
    private final static AtomicInteger COUNT = new AtomicInteger(0);

    public static int get( )
    {
        return COUNT.get();
    }

    public static int set( int value )
    {
        return COUNT.getAndSet( value );
    }

    public static int increment( )
    {
        return COUNT.incrementAndGet();
    }
}
