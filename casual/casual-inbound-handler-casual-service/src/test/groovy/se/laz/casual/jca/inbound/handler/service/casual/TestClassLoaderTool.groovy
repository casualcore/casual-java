/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual

class TestClassLoaderTool
{
    /**
     * Create a Classloader that has only the following jar files.
     *
     * @param jars
     * @return classloader
     */
    static ClassLoader createClassLoader( File...jars )
    {
        URL[] resources = new URL[jars.length]
        for (int i; i < jars.length; i++)
        {
            File f = jars[i]
            resources[i] = f.toURI().toURL()
        }
        return new URLClassLoader(resources, (ClassLoader) null)
    }
}
