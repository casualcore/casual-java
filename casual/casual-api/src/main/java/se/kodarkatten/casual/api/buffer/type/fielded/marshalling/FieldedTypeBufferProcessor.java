package se.kodarkatten.casual.api.buffer.type.fielded.marshalling;

import se.kodarkatten.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details.Marshaller;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.details.Unmarshaller;

import java.util.Objects;

/**
 * marshall/unmarshall FieldedTypeBuffer
 * methods or fields annotated with @CasualFieldElement
 * will be taken into consideration
 *
 */
public final class FieldedTypeBufferProcessor
{
    private FieldedTypeBufferProcessor()
    {}

    public static FieldedTypeBuffer marshall(final Object o)
    {
        Objects.requireNonNull(o, "object to be marshalled is not allowed to be null");
        FieldedTypeBuffer b = FieldedTypeBuffer.create();
        return Marshaller.writeFields(o, b);
    }

    /**
     * Note, clazz needs a no op constructor
     * It does not have to be public though
     * @param b
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T unmarshall(final FieldedTypeBuffer b, final Class<T> clazz)
    {
        Objects.requireNonNull(b, "fielded type buffer is not allowed to be null");
        Objects.requireNonNull(clazz, "clazz is not allowed to be null");
        return Unmarshaller.createObject(b, clazz);
    }

}
