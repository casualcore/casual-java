package se.kodarkatten.casual.api.buffer.type;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.CasualBufferType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        JsonBuffer that = (JsonBuffer) o;

        if( that.payload.size() != this.payload.size() )
        {
            return false;
        }

        for( int i = 0; i< payload.size(); i++ )
        {
            if( ! Arrays.equals( payload.get( i ), that.payload.get( i ) ) )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = 1;

        for( byte[] b: payload )
        {
            result = result * 31 + Arrays.hashCode( b );
        }

        return result;
    }
}
