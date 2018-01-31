package se.kodarkatten.casual.api.buffer;

import java.util.Arrays;
import java.util.Optional;

public enum CasualBufferType
{
    JSON(".json/"),
    JSON_JSCD(".json/jscd"),
    FIELDED("CFIELD/");
    private final String name;
    CasualBufferType(final String name)
    {
        this.name = name;
    }
    public static CasualBufferType unmarshall(final String name)
    {
        Optional<CasualBufferType> t = Arrays.stream(CasualBufferType.values())
                                             .filter(v -> v.getName().equals(name))
                                             .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException("CasualBufferType:" + name));
    }

    public static String marshall(CasualBufferType type)
    {
        return type.getName();
    }

    public String getName()
    {
        return name;
    }

}
