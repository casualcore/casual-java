/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.external.json.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import se.laz.casual.api.external.json.JsonProvider;

import java.io.Reader;

/**
 * JsonProvider using gson
 */
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
