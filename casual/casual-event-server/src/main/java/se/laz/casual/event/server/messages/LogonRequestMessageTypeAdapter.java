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

public class LogonRequestMessageTypeAdapter implements JsonDeserializer<LogonRequestMessage>
{
    private static final Logger log = Logger.getLogger(LogonRequestMessageTypeAdapter.class.getName());
    public static LogonRequestMessageTypeAdapter of()
    {
        return new LogonRequestMessageTypeAdapter();
    }
    @Override
    public LogonRequestMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        String message = json.getAsJsonObject().get("message").getAsString();
        log.finest(() -> "message: " + message);
        LogonRequest logonRequest = LogonRequest.unmarshall(message);
        return LogonRequestMessage.of(logonRequest);
    }
}
