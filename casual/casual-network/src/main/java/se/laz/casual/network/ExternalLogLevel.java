package se.laz.casual.network;

import se.laz.casual.api.CasualTypeException;

import java.util.Arrays;
import java.util.Objects;

public enum ExternalLogLevel
{
    TRACE("TRACE"),
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");
    private String level;
    ExternalLogLevel(String level)
    {
        this.level = level;
    }

    public static ExternalLogLevel unmarshall(String level)
    {
        Objects.requireNonNull(level, "level can not be null");
        return Arrays.stream(values())
                     .filter(value -> value.getLevel().equals(level))
                     .findFirst()
                     .orElseThrow(() -> new CasualTypeException("No match for level: " + level));
    }

    public String getLevel()
    {
        return level;
    }

}
