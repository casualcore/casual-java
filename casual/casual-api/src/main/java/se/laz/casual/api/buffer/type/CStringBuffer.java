package se.laz.casual.api.buffer.type;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.CasualBufferType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * {@link CasualBuffer} for type {@link CasualBufferType#CSTRING}
 */
public class CStringBuffer implements CasualBuffer
{
    private static final long serialVersionUID = 1L;
    private final byte[] payload;
    private CStringBuffer(byte[] payload)
    {
        this.payload = payload;
    }

    /**
     * Creates a {@link CStringBuffer}
     * @param value - the string value
     * @return a {@link CStringBuffer}
     */
    public static CStringBuffer of(String value)
    {
        Objects.requireNonNull(value, "value should not be null!");
        if(value.endsWith("\0"))
        {
            throw new IllegalArgumentException("String should not be nullterminated!");
        }
        value += "\0";
        return new CStringBuffer(value.getBytes());
    }
    /**
     * Creates a {@link CStringBuffer}
     * @param payload has to contain one byte[] which has to be a null terminated cstring using the default platform encoding
     * @return a {@link CStringBuffer}
     */
    public static CStringBuffer of(final List<byte[]> payload)
    {
        Objects.requireNonNull(payload, "payload can not be null!");
        // Java string can only be created from one byte[] - ie that is the max size of a javastring
        // The payload is expected to be a null terminated c string
        if(payload.size() != 1)
        {
            throw new IllegalArgumentException("the list has to contain only one byte[]");
        }
        return new CStringBuffer(payload.get(0));
    }
    @Override
    public String getType()
    {
        return CasualBufferType.CSTRING.getName();
    }
    @Override
    public List<byte[]> getBytes()
    {
        return Arrays.asList(payload);
    }
    @Override
    public String toString()
    {
        return new String(payload, 0,payload.length - 1);
    }
}
