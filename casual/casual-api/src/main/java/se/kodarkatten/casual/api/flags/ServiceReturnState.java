package se.kodarkatten.casual.api.flags;

/**
 * @author jone
 */
public enum ServiceReturnState
{
    TPFAIL(0x0001),
    TPSUCCESS(0x0002);

    private final int value;

    ServiceReturnState(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}