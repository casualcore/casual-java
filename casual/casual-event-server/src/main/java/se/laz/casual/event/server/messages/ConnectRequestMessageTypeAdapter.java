/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server.messages;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.logging.Logger;

public class ConnectRequestMessageTypeAdapter implements JsonDeserializer<ConnectRequestMessage>
{
    private static final Logger log = Logger.getLogger(ConnectRequestMessageTypeAdapter.class.getName());
    public static ConnectRequestMessageTypeAdapter of()
    {
        return new ConnectRequestMessageTypeAdapter();
    }
    @Override
    public ConnectRequestMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        String message = json.getAsJsonObject().get("message").getAsString();
        log.finest(() -> "message: " + message);
        ConnectRequest connectRequest = ConnectRequest.unmarshall(message);
        return ConnectRequestMessage.of(connectRequest);
    }
}
