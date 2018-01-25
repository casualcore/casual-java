package se.kodarkatten.casual.connection.lookup;

import se.kodarkatten.casual.connection.lookup.util.ConfigurationUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import java.util.List;

import static se.kodarkatten.casual.connection.lookup.util.ConfigurationUtils.getReader;
import static se.kodarkatten.casual.connection.lookup.util.ConfigurationUtils.getURI;

@Singleton
public class ConfigurationProvider
{
    private Configuration config;
    public static final String SYSTEM_PROPERTY_NAME = "casual.connection.lookup.config";

    public List<String> getCasualJNDINames()
    {
        return config.getJNDINames();
    }

    @PostConstruct
    public void initialize()
    {
        config = ConfigurationUtils.slurpJSON(getReader(getURI(SYSTEM_PROPERTY_NAME)));
    }
}
