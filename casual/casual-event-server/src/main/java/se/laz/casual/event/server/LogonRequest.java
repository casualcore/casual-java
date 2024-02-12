package se.laz.casual.event.server;

import se.laz.casual.api.CasualRuntimeException;

import java.util.Arrays;

public enum LogonRequest
{
    REQUEST("HELLO");
    private final String value;
    LogonRequest(String value)
    {
        this.value = value;
    }
    static LogonRequest unmarshall(String in)
    {
        return Arrays.stream(values())
                     .filter(v -> v.value.equals(in))
                     .findFirst()
                     .orElseThrow(() -> new CasualRuntimeException("Mismatch for value: " + in));
    }
}
