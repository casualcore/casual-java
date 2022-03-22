/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

import se.laz.casual.api.CasualTypeException;

import java.util.Arrays;
import java.util.Optional;

/**
 * The field types that are supported by casual
 */
public enum FieldType
{
    CASUAL_FIELD_SHORT(1, "short", Short.class),
    CASUAL_FIELD_LONG(2, "long", Long.class),
    CASUAL_FIELD_CHAR(3, "char", Character.class),
    CASUAL_FIELD_FLOAT(4, "float", Float.class),
    CASUAL_FIELD_DOUBLE(5, "double", Double.class),
    CASUAL_FIELD_STRING(6, "string", String.class),
    // note, array type
    CASUAL_FIELD_BINARY(7, "binary", byte[].class);
    private final int v;
    private final String name;
    private final Class<?> clazz;
    private static final String FIELD_TYPE = "FieldType: ";
    private static final Short DEFAULT_VALUE_SHORT = 0;
    private static final Long DEFAULT_VALUE_LONG = 0L;
    private static final Integer DEFAULT_VALUE_INTEGER = 0;
    private static final Character DEFAULT_VALUE_CHAR = Character.MIN_VALUE;
    private static final Float DEFAULT_VALUE_FLOAT = 0.0f;
    private static final Double DEFAULT_VALUE_DOUBLE = 0.0d;
    private static final String DEFAULT_VALUE_STRING = "";
    private static final byte[] DEFAULT_VALUE_BINARY = new byte[0];

    FieldType(int v, String name, Class<?> clazz)
    {
        this.v = v;
        this.name = name;
        this.clazz = clazz;
    }

    /**
     * Unmarshall to FieldType
     * @throws IllegalArgumentException if the type is unknown
     * @param type the type
     * @return the field type
     */
    public static FieldType unmarshall(int type)
    {
        Optional<FieldType> t = Arrays.stream(FieldType.values())
                                      .filter(n -> n.getValue() == type)
                                      .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException(FIELD_TYPE + type));
    }
    /**
     * Unmarshall to FieldType
     * @throws IllegalArgumentException if the type is unknown
     * @param type the type
     * @return the field type
     */
    public static FieldType unmarshall(String type)
    {
        Optional<FieldType> t = Arrays.stream(FieldType.values())
                                      .filter(n -> n.getName().equals(type))
                                      .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException(FIELD_TYPE + type));
    }
    /**
     * Unmarshall to FieldType
     * @throws IllegalArgumentException if the type is unknown
     * @param type the type
     * @return the field type
     */
    public static FieldType unmarshall(Class<?> type)
    {
        Optional<FieldType> t = Arrays.stream(FieldType.values())
                                      .filter(n -> n.getClazz().equals(type))
                                      .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException(FIELD_TYPE + type));
    }

    /**
     * Predicate to check if type is of any known FieldType
     * @param type the type
     * @return true if matching, false if not
     */
    public static boolean isOfFieldType(Class<?> type)
    {
        return Arrays.stream(FieldType.values())
                     .filter(n -> n.getClazz().equals(type))
                     .map(v -> true)
                     .findFirst()
                     .orElse(false);
    }

    /**
     * Marshall a FieldType into it's int representation
     * @param f the type
     * @return the marshalled representation
     */
    public static int marshall(FieldType f)
    {
        return f.getValue();
    }

    /**
     * @return the value
     */
    public int getValue()
    {
        return v;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the class
     */
    public Class<?> getClazz()
    {
        return clazz;
    }

    @SuppressWarnings("unchecked")
    public <T> T defaultValue()
    {
        switch(this)
        {
            case CASUAL_FIELD_LONG:
                return (T)DEFAULT_VALUE_LONG;
            case CASUAL_FIELD_FLOAT:
                return (T)DEFAULT_VALUE_FLOAT;
            case CASUAL_FIELD_DOUBLE:
                return (T)DEFAULT_VALUE_DOUBLE;
            case CASUAL_FIELD_CHAR:
                return (T)DEFAULT_VALUE_CHAR;
            case CASUAL_FIELD_SHORT:
                return (T)DEFAULT_VALUE_SHORT;
            case CASUAL_FIELD_BINARY:
                return (T)DEFAULT_VALUE_BINARY;
            case CASUAL_FIELD_STRING:
                return (T)DEFAULT_VALUE_STRING;
            default:
                throw new CasualTypeException("No defaultValue available for: " + this);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T defaultValueInteger()
    {
        return (T)DEFAULT_VALUE_INTEGER;
    }
}
