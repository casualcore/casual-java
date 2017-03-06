package se.kodarkatten.casual.api.flags;

/**
 * @author jone
 */
public enum Events
{
    TPEV_DISCONIMM(0x0001),
    TPEV_SVCERR(0x0002),
    TPEV_SVCFAIL(0x0004),
    TPEV_SVCSUCC(0x0008),
    TPEV_SENDONLY(0x0020);

    private final int value;

    Events(final int value)
    {
        this.value = value;
    }

    public final int getValue()
    {
        return value;
    }
}