package se.laz.casual.connection.caller;

import se.laz.casual.api.CasualRuntimeException;

public class CasualCallerException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1l;
    public CasualCallerException()
    {
        super();
    }

    public CasualCallerException(String message)
    {
        super(message);
    }

    public CasualCallerException(Throwable t)
    {
        super(t);
    }

    public CasualCallerException(String message, Throwable t)
    {
        super(message, t);
    }
}
