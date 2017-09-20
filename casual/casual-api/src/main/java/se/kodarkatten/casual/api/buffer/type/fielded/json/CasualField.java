package se.kodarkatten.casual.api.buffer.type.fielded.json;

import se.kodarkatten.casual.api.buffer.type.fielded.FieldType;

import java.util.Objects;

public final class CasualField
{
    private final String name;
    int id;
    String type;
    FieldType fieldtype;
    long realId;

    private CasualField(long realId, final String name, FieldType fieldtype)
    {
        this.realId = realId;
        this.name = name;
        this.fieldtype = fieldtype;
    }

    public static CasualField of(long realId, final String name, FieldType t)
    {
        Objects.requireNonNull(name, "name is not allowed to be null");
        return new CasualField(realId, name, t);
    }

    public String getName()
    {
        return name;
    }

    public FieldType getType()
    {
        return fieldtype;
    }

    public long getRealId()
    {
        return realId;
    }
}

