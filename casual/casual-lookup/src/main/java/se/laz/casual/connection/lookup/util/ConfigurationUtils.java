/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup.util;

import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.connection.lookup.CasualLookupException;
import se.laz.casual.connection.lookup.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

public final class ConfigurationUtils
{
    private ConfigurationUtils()
    {}

    public static Configuration slurpJSON(final Reader r)
    {
        return JsonProviderFactory.getJsonProvider().fromJson(r, Configuration.class);
    }

    public static URI getURI(String propertyName)
    {
        String v = System.getProperty(propertyName);
        if(null == v)
        {
            throw new CasualLookupException("missing system property: " + propertyName);
        }
        try
        {
            return new URI("file:///" + v);
        }
        catch (URISyntaxException e)
        {
            throw new CasualLookupException(e);
        }
    }

    public static Reader getReader(final URI resource)
    {
        try
        {
            return new FileReader(new File(resource));
        }
        catch (FileNotFoundException e)
        {
            throw new CasualLookupException("resource: " + resource + " not found", e);
        }
    }
}
