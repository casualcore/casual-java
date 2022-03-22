package se.laz.casual.api.buffer.type.fielded;

public final class FieldedTestBufferWriter
{
    public static FieldedTypeBuffer write(FieldedTypeBuffer buffer, String name, Short value)
    {
        buffer.write(name, value);
        return buffer;
    }
    public static FieldedTypeBuffer write(FieldedTypeBuffer buffer, String name, Long value)
    {
        buffer.write(name, value);
        return buffer;
    }
    public static FieldedTypeBuffer write(FieldedTypeBuffer buffer, String name, Float value)
    {
        buffer.write(name, value);
        return buffer;
    }
    public static FieldedTypeBuffer write(FieldedTypeBuffer buffer, String name, Double value)
    {
        buffer.write(name, value);
        return buffer;
    }
    /*
    public static FieldedTypeBuffer write(FieldedTypeBuffer buffer, String name, Character value)
    {
        buffer.write(name, value);
        return buffer;
    }
    public static FieldedTypeBuffer write(FieldedTypeBuffer buffer, String name, String value)
    {
        buffer.write(name, value);
        return buffer;
    }*/
    public static FieldedTypeBuffer write(FieldedTypeBuffer buffer, String name, byte[] value)
    {
        buffer.write(name, value);
        return buffer;
    }
}
