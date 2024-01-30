package se.laz.casual.network;

import io.netty.handler.logging.LogLevel;

import java.util.Optional;

public final class LogLevelProvider
{
    private static final String DEFAULT_LOGGING_LEVEL = "INFO";
    public static String CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL_NAME = "CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL";
    public static String CASUAL_INBOUND_NETTY_LOGGING_LEVEL_NAME = "CASUAL_INBOUND_NETTY_LOGGING_LEVEL";
    public static String CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL_NAME = "CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL";
    public static LogLevel REVERSE_LOGGING_LEVEL = getOrDefault(CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL_NAME);
    public static LogLevel INBOUND_LOGGING_LEVEL = getOrDefault(CASUAL_INBOUND_NETTY_LOGGING_LEVEL_NAME);
    public static LogLevel OUTBOUND_LOGGING_LEVEL = getOrDefault(CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL_NAME);

    private static LogLevel getOrDefault(String envName)
    {
        ExternalLogLevel externalLogLevel = ExternalLogLevel.unmarshall(Optional.ofNullable(System.getenv(envName)).orElse(DEFAULT_LOGGING_LEVEL));
        return LogLevelMapper.map(externalLogLevel);
    }
}
