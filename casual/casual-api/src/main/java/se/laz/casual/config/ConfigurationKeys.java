/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config;

import java.util.List;

public class ConfigurationKeys
{
    public static final ConfigurationKey<String> CASUAL_API_FIELDED_ENCODING = new ConfigurationKey<>( "CASUAL_API_FIELDED_ENCODING" );
    public static final ConfigurationKey<Mode> CASUAL_INBOUND_STARTUP_MODE = new ConfigurationKey<>( "CASUAL_INBOUND_STARTUP_MODE" );
    public static final ConfigurationKey<List<String>> CASUAL_INBOUND_STARTUP_SERVICES = new ConfigurationKey<>( "CASUAL_INBOUND_STARTUP_SERVICES" );
}
