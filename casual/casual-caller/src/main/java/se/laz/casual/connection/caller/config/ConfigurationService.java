/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.config;

import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.config.ConfigurationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Optional;

public class ConfigurationService
{
    public static final String CASUAL_CALLER_CONFIG_FILE_ENV_NAME = "CASUAL_CALLER_CONFIG_FILE";

    private static final ConfigurationService instance = new ConfigurationService();

    private final Configuration configuration;

    ConfigurationService()
    {
        this.configuration = init();
    }

    public static ConfigurationService getInstance()
    {
        return instance;
    }

    private Configuration init()
    {
        return getEnv(CASUAL_CALLER_CONFIG_FILE_ENV_NAME)
                .map(this::buildConfigurationFromFile)
                .orElse(buildConfigurationFromEnvs());
    }

    private Optional<String> getEnv(String name)
    {
        return Optional.ofNullable(System.getenv(name));
    }

    private Configuration buildConfigurationFromFile(String file)
    {
        try
        {
            return JsonProviderFactory.getJsonProvider().fromJson(new FileReader(file), Configuration.class);
        }
        catch (FileNotFoundException e)
        {
            throw new ConfigurationException("Could not find configuration file '" + file + "' for casual-caller.");
        }
    }

    private Configuration buildConfigurationFromEnvs()
    {
        return Configuration.fromEnvOrDefaults();
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }
}
