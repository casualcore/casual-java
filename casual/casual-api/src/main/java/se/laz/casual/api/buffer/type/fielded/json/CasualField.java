/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.json;

import se.laz.casual.api.buffer.type.fielded.FieldType;

import java.util.Objects;

/**
 * A casual field
 */
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

    /**
     * Create a casual field
     * @param realId the real id
     * @param name the name
     * @param t the field type
     * @return a CasualField
     */
    public static CasualField of(long realId, final String name, FieldType t)
    {
        Objects.requireNonNull(name, "name is not allowed to be null");
        return new CasualField(realId, name, t);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the type
     */
    public FieldType getType()
    {
        return fieldtype;
    }

    /**
     * @return the real id
     */
    public long getRealId()
    {
        return realId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CasualField that = (CasualField) o;
        return id == that.id &&
                realId == that.realId &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                fieldtype == that.fieldtype;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, id, type, fieldtype, realId);
    }

    @Override
    public String toString()
    {
        return "CasualFieldElement{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", type='" + type + '\'' +
                ", fieldtype=" + fieldtype +
                ", realId=" + realId +
                '}';
    }
}
