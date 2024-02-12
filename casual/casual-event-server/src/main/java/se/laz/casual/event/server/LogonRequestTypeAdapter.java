package se.laz.casual.event.server;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.logging.Logger;

public class LogonRequestTypeAdapter implements JsonDeserializer<LogonRequestMessage>
{
    private static final Logger log = Logger.getLogger(LogonRequestTypeAdapter.class.getName());
    public static LogonRequestTypeAdapter of()
    {
        return new LogonRequestTypeAdapter();
    }
    @Override
    public LogonRequestMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        String message = json.getAsJsonObject().get("message").getAsString();
        log.info(() -> "message: " + message);
        LogonRequest logonRequest = LogonRequest.unmarshall(message);
        return LogonRequestMessage.of(logonRequest);
    }
}
