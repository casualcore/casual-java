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
    private static final String NULL_TERMINATOR = "\0";
    private static final byte NULL_TERMINATOR_BYTE = NULL_TERMINATOR.getBytes()[0];

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
    public static CStringBuffer of(final String value)
    {
        Objects.requireNonNull(value, "value should not be null!");
        String data = value;
        if(!data.endsWith( NULL_TERMINATOR ) )
        {
            data += NULL_TERMINATOR;
        }
        return new CStringBuffer(data.getBytes());
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
        byte[] data = payload.get( 0 );
        if( data[data.length-1] != NULL_TERMINATOR_BYTE )
        {
            throw new IllegalArgumentException("the byte[] must be null terminated.");
        }
        return new CStringBuffer(data);
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
