package se.kodarkatten.casual.connection.lookup;

import se.kodarkatten.casual.api.CasualRuntimeException;

public class CasualLookupException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1l;
    public CasualLookupException(String msg)
    {
        super(msg);
    }
    public CasualLookupException(Throwable t)
    {
        super(t);
    }
    public CasualLookupException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
