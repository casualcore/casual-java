package se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details;

import java.util.Objects;

public class ParameterInfo
{
    private final Class<?> type;
    protected ParameterInfo(final Class<?> type)
    {
        this.type = type;
    }
    public static ParameterInfo of(final Class<?> type)
    {
        Objects.requireNonNull(type);
        return new ParameterInfo(type);
    }

    public Class<?> getType()
    {
        return type;
    }
}
