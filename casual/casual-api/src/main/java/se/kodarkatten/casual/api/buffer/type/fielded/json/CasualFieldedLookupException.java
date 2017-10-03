package se.kodarkatten.casual.api.buffer.type.fielded.json;

import se.kodarkatten.casual.api.CasualRuntimeException;

public class CasualFieldedLookupException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualFieldedLookupException(final String msg)
    {
        super(msg);
    }
    public CasualFieldedLookupException(final Throwable t)
    {
        super(t);
    }
    public CasualFieldedLookupException(final String msg, final Throwable t)
    {
        super(msg, t);
    }
}
