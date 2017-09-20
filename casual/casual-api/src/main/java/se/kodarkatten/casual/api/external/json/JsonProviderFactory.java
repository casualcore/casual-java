package se.kodarkatten.casual.api.external.json;

import se.kodarkatten.casual.api.external.json.impl.GsonProvider;

public final class JsonProviderFactory
{
    private JsonProviderFactory()
    {}
    public static JsonProvider getJsonProvider()
    {
        return new GsonProvider();
    }
}
