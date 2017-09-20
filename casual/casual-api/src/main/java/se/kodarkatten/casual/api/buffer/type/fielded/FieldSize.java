package se.kodarkatten.casual.api.buffer.type.fielded;

public enum FieldSize
{
    FIELD_ID(8),
    FIELD_SIZE(8);
    private final int size;
    FieldSize(int size)
    {
        this.size = size;
    }
    public int getSize()
    {
        return size;
    }
}
