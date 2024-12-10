/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class StaticLoader
{
    public static Class<StaticData> getInstance()
    {
        return StaticData.class;
    }

    @SuppressWarnings( "unchecked" )
    public static Class<StaticData> createInstance()
    {
        try( URLClassLoader classLoader = new URLClassLoader( new URL[] {}, StaticLoader.getInstance().getClassLoader() ) )
        {
            return (Class<StaticData>) classLoader.loadClass( StaticData.class.getName() );
        }
        catch( ClassNotFoundException | IOException e )
        {
            throw new RuntimeException( "Failed to initialise class.", e );
        }
    }

    @SuppressWarnings( "unchecked" )
    public static Class<StaticData> createNewInstance()
    {
        try( URLClassLoader classLoader = createClassLoader() )
        {
            return (Class<StaticData>) classLoader.loadClass( StaticData.class.getName() );
        }
        catch( ClassNotFoundException | IOException e )
        {
            throw new RuntimeException( "Failed to initialise class.", e );
        }
    }

    private static URLClassLoader createClassLoader( )
    {
        URL[] urls = null;
        try
        {
            File f = new File("build/classes/java/main/");
            URL url = f.toURI().toURL();
            urls = new URL[] { url };
        }
        catch( MalformedURLException e )
        {
            throw new RuntimeException( "Invalid URL for classloader.", e );
        }
        return new URLClassLoader( urls, null );
    }
}
