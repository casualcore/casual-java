/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.external.json.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import se.laz.casual.api.buffer.type.JavaServiceCallDefinition;

import java.lang.reflect.Type;

public final class GsonJscdTypeAdapter implements JsonDeserializer<JavaServiceCallDefinition>
{
    @Override
    public JavaServiceCallDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    {
        Gson g = new Gson();
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement values = jsonObject.remove(JavaServiceCallDefinition.METHOD_PARAMS);
        JavaServiceCallDefinition result = g.fromJson( jsonObject, JavaServiceCallDefinition.class );

        JsonArray valueArray = values.getAsJsonArray();
        String[] methodParamTypes = result.getMethodParamTypes();
        Object[] paramValues = new Object[valueArray.size()];

        for( int i=0;i<valueArray.size();i++ )
        {
            JsonElement e = valueArray.get( i );
            try
            {
                Class<?> paramClass = Class.forName( methodParamTypes[i], true, Thread.currentThread().getContextClassLoader() );
                paramValues[i] = g.fromJson( e, paramClass );
            } catch (ClassNotFoundException e1)
            {
                throw new JsonParseException( "Unable to find class.", e1 );
            }
        }
        result.setMethodParams( paramValues );
        return result;
    }
}
