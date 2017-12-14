package se.kodarkatten.casual.api.buffer.type.fielded.marshalling;

/**
 * STRICT mode:
 * marshalling - if a value is null, FieldedMarshallingException will be thrown
 * unmarshalling - if a value is missing in the buffer, FieldedUnmarshallingException will be thrown
 *
 * RELAXED mode:
 * marshalling - if a value is null nothing happens except there will be no marshalled value
 * unmarshalling - if a value is missing in the buffer - no initialization will take place for that value
 */
public enum FieldedTypeBufferProcessorMode
{
    STRICT,
    RELAXED
}
