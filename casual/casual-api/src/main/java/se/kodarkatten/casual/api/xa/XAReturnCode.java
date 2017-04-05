package se.kodarkatten.casual.api.xa;

/**
 * Created by aleph on 2017-03-30.
 */
public enum XAReturnCode
{
    XA_RBBASE(100),
    XA_RBROLLBACK(XA_RBBASE.id),
    XA_RBCOMMFAIL(XA_RBBASE.id + 1),
    XA_RBDEADLOCK(XA_RBBASE.id + 2),
    XA_RBINTEGRITY(XA_RBBASE.id + 3),
    XA_RBOTHER(XA_RBBASE.id + 4),
    XA_RBPROTO(XA_RBBASE.id + 5),
    XA_RBTIMEOUT(XA_RBBASE.id + 6),
    XA_RBTRANSIENT(XA_RBBASE.id + 6),
    XA_RBEND(XA_RBTRANSIENT.id),
    XA_NOMIGRATE(9),
    XA_HEURHAZ(8),
    XA_HEURCOM(7),
    XA_HEURRB(6),
    XA_HEURMIX(5),
    XA_RETRY(4),
    XA_RDONLY(3),
    XA_OK(0),
    XAER_ASYNC(-2),
    XAER_RMERR(-3),
    XAER_NOTA(-4),
    XAER_INVAL(-5),
    XAER_PROTO(-6),
    XAER_RMFAIL(-7),
    XAER_DUPID(-8),
    XAER_OUTSIDE(-9);
    private int id;
    XAReturnCode(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public static final int marshal(XAReturnCode xa)
    {
        return xa.id;
    }

    public static final XAReturnCode unmarshal(int id)
    {
        for (XAReturnCode c : XAReturnCode.values())
        {
            if (c.id == id)
            {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown XAReturnCode:" + id);
    }
}
