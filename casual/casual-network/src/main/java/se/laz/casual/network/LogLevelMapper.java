/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network;

import io.netty.handler.logging.LogLevel;
import se.laz.casual.api.CasualTypeException;

public final class LogLevelMapper
{
    private LogLevelMapper()
    {}
    public static LogLevel map(ExternalLogLevel level)
    {
        switch(level)
        {
            case INFO:
                return LogLevel.INFO;
            case DEBUG:
                return LogLevel.DEBUG;
            case WARN:
                return LogLevel.WARN;
            case TRACE:
                return LogLevel.TRACE;
            case ERROR:
                return LogLevel.ERROR;
            default:
                throw new CasualTypeException("No mapping from: " + level + " to nettys LogLevel");
        }
    }
}
