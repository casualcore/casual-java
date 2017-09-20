package se.kodarkatten.casual.api.buffer.type.fielded;

public interface FieldedData<T>
{
    T getData();
    FieldType getType();
}
