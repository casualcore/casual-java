/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.internal.thread;

public class ThreadClassLoaderTool
{
    private final ClassLoader initial;

    public ThreadClassLoaderTool()
    {
        initial = Thread.currentThread().getContextClassLoader();
    }

    public void loadClassLoader(Object object )
    {
        ClassLoader c = object.getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader( c );
    }

    public void revertClassLoader()
    {
        Thread.currentThread().setContextClassLoader( initial );
    }

}
