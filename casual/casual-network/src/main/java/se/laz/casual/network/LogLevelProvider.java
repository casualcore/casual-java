/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network;

import io.netty.handler.logging.LogLevel;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;

public final class LogLevelProvider
{
    public static final LogLevel REVERSE_LOGGING_LEVEL = toLogLevel( ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL ) );
    public static final LogLevel INBOUND_LOGGING_LEVEL = toLogLevel( ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL  ) );
    public static final LogLevel OUTBOUND_LOGGING_LEVEL = toLogLevel( ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL) );

    private LogLevelProvider()
    {}

    public static LogLevel toLogLevel( String level )
    {
        ExternalLogLevel externalLogLevel = ExternalLogLevel.unmarshall( level );
        return LogLevelMapper.map(externalLogLevel);
    }
}
