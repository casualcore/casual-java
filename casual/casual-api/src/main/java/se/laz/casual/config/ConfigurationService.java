/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import se.laz.casual.api.external.json.JsonProviderFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Optional;

/**
 * Singleton access to Casual Configuration.
 */
public class ConfigurationService
{

    public static final String CASUAL_CONFIG_FILE_ENV_NAME = "CASUAL_CONFIG_FILE";
    public static final String CASUAL_INBOUND_STARTUP_MODE_ENV_NAME = "CASUAL_INBOUND_STARTUP_MODE";

    private static final ConfigurationService INSTANCE = new ConfigurationService();

    private final Configuration configuration;

    private ConfigurationService()
    {
        this.configuration = init();
    }

    public static final ConfigurationService getInstance()
    {
        return INSTANCE;
    }

    private Configuration init()
    {
        return getEnv( CASUAL_CONFIG_FILE_ENV_NAME )
                .map( this::buildConfigurationFromFile )
                .orElse( buildConfigurationFromEnvs() );
    }

    private Optional<String> getEnv( String name )
    {
        return Optional.ofNullable( System.getenv( name ) );
    }

    private Configuration buildConfigurationFromFile( String file )
    {
        try
        {
            return JsonProviderFactory.getJsonProvider().fromJson( new FileReader( file ), Configuration.class );
        }
        catch( FileNotFoundException e  )
        {
            throw new ConfigurationException( "Could not find configuration file specified.", e );
        }
    }

    private Configuration buildConfigurationFromEnvs( )
    {
        Mode mode = getEnv( CASUAL_INBOUND_STARTUP_MODE_ENV_NAME )
                .map( name -> name.isEmpty() ? Mode.IMMEDIATE : Mode.fromName( name ) )
                .orElse( Mode.IMMEDIATE );
        return Configuration.newBuilder()
                .withDomain( Domain.getFromEnv() )
                .withInbound( Inbound.newBuilder()
                        .withStartup( Startup.newBuilder()
                                .withMode( mode )
                                .build() )
                        .build() )
                .build();
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }
}
