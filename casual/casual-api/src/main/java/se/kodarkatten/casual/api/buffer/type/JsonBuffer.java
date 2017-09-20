package se.kodarkatten.casual.api.buffer.type;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.CasualBufferType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonBuffer implements CasualBuffer
{
    private final List<byte[]> payload;
    private JsonBuffer(final List<byte[]> payload)
    {
        this.payload = payload;
    }
    public static JsonBuffer of(final List<byte[]> payload)
    {
        Objects.requireNonNull(payload, "payload is null, this is nonsense");
        return new JsonBuffer(payload);
    }

    public static JsonBuffer of(final String json)
    {
        Objects.requireNonNull(json, "json is null, this is nonsense");
        final List<byte[]> jsonPayload = new ArrayList<>();
        jsonPayload.add(toBytes(json));
        return new JsonBuffer(jsonPayload);
    }

    @Override
    public String getType()
    {
        return CasualBufferType.JSON.getName();
    }

    @Override
    public List<byte[]> getBytes()
    {
        return payload;
    }

    @Override
    public String toString()
    {
        return payload.stream()
                      .map(b -> new String(b, StandardCharsets.UTF_8))
                      .collect(Collectors.joining(""));
    }

    private static byte[] toBytes(final String json)
    {
        return json.getBytes(StandardCharsets.UTF_8);
    }

}
