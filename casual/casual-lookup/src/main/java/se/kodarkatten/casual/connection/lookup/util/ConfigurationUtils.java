package se.kodarkatten.casual.connection.lookup.util;

import se.kodarkatten.casual.api.external.json.JsonProviderFactory;
import se.kodarkatten.casual.connection.lookup.CasualLookupException;
import se.kodarkatten.casual.connection.lookup.Configuration;

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
