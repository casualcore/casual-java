/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup;

import se.laz.casual.connection.lookup.util.ConfigurationUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import java.util.List;

import static se.laz.casual.connection.lookup.util.ConfigurationUtils.getReader;
import static se.laz.casual.connection.lookup.util.ConfigurationUtils.getURI;

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
