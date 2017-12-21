package se.kodarkatten.casual.api.buffer.type.fielded;

import java.util.Arrays;
import java.util.Optional;

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
    FieldType(int v, String name, Class<?> clazz)
    {
        this.v = v;
        this.name = name;
        this.clazz = clazz;
    }
    public static FieldType unmarshall(int type)
    {
        Optional<FieldType> t = Arrays.stream(FieldType.values())
                                      .filter(n -> n.getValue() == type)
                                      .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException(FIELD_TYPE + type));
    }
    public static FieldType unmarshall(String type)
    {
        Optional<FieldType> t = Arrays.stream(FieldType.values())
                                      .filter(n -> n.getName().equals(type))
                                      .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException(FIELD_TYPE + type));
    }

    public static FieldType unmarshall(Class<?> type)
    {
        Optional<FieldType> t = Arrays.stream(FieldType.values())
                                      .filter(n -> n.getClazz().equals(type))
                                      .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException(FIELD_TYPE + type));
    }

    public static boolean isOfFieldType(Class<?> type)
    {
        return Arrays.stream(FieldType.values())
                     .filter(n -> n.getClazz().equals(type))
                     .map(v -> true)
                     .findFirst()
                     .orElse(false);
    }

    public static int marshall(FieldType f)
    {
        return f.getValue();
    }
    public int getValue()
    {
        return v;
    }
    public String getName()
    {
        return name;
    }
    public Class<?> getClazz()
    {
        return clazz;
    }
}
