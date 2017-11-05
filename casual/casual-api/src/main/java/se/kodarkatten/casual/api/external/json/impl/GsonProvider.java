package se.kodarkatten.casual.api.external.json.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    @Override
    public <T> T fromJson(String s, Class<T> clazz)
    {
        Gson gson = new Gson();
        return gson.fromJson(s, clazz);
    }

    @Override
    public <T> T fromJson(String s, Class<T> clazz, Object typeAdapter)
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter( clazz, typeAdapter );
        Gson gson = builder.create();
        return gson.fromJson(s, clazz);
    }

    @Override
    public String toJson( Object object )
    {
        Gson gson = new Gson();
        return gson.toJson( object );
    }
}
