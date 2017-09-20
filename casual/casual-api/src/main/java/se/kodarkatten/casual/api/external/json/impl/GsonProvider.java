package se.kodarkatten.casual.api.external.json.impl;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import se.kodarkatten.casual.api.external.json.JsonProvider;

import java.io.Reader;

public final class GsonProvider implements JsonProvider
{
    @Override
    public <T> T fromJson(Reader r, Class<T> clazz)
    {
        Gson gson = new Gson();
        return gson.fromJson(new JsonReader(r), clazz);
    }
}
