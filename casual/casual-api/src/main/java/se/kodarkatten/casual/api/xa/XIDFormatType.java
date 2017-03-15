package se.kodarkatten.casual.api.xa;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by aleph on 2017-03-15.
 */
public enum XIDFormatType
{
    NULL(-1),
    OSI(0);
    private final int type;
    XIDFormatType(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    public static int marshal(XIDFormatType t)
    {
        return t.getType();
    }

    public static final XIDFormatType unmarshal(int n)
    {
        Optional<XIDFormatType> t = Arrays.stream(XIDFormatType.values())
                                          .filter(v -> v.getType() == n)
                                          .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException("TransactionType:" + n));
    }
}
