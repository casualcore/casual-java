/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network;

import io.netty.handler.logging.LogLevel;

import java.util.Optional;

public final class LogLevelProvider
{
    public static final String DEFAULT_LOGGING_LEVEL = "INFO";
    public static final String CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL_NAME = "CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL";
    public static final String CASUAL_INBOUND_NETTY_LOGGING_LEVEL_NAME = "CASUAL_INBOUND_NETTY_LOGGING_LEVEL";
    public static final String CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL_NAME = "CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL";
    public static final LogLevel REVERSE_LOGGING_LEVEL = getOrDefault(CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL_NAME);
    public static final LogLevel INBOUND_LOGGING_LEVEL = getOrDefault(CASUAL_INBOUND_NETTY_LOGGING_LEVEL_NAME);
    public static final LogLevel OUTBOUND_LOGGING_LEVEL = getOrDefault(CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL_NAME);

    private LogLevelProvider()
    {}

    public static LogLevel getOrDefault(String envName)
    {
        ExternalLogLevel externalLogLevel = ExternalLogLevel.unmarshall(Optional.ofNullable(getEnv(envName)).orElse(DEFAULT_LOGGING_LEVEL));
        return LogLevelMapper.map(externalLogLevel);
    }

    private static String getEnv(String envName)
    {
        String value = System.getenv(envName);
        if(null != value && value.isEmpty())
        {
            value = null;
        }
        return value;
    }
}
