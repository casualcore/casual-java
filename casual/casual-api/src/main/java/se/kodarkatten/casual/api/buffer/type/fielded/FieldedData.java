package se.kodarkatten.casual.api.buffer.type.fielded;

public interface FieldedData<T>
{
    T getData();

    <T> T getData(Class<T> clazz);

    FieldType getType();
}
