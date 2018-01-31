package se.kodarkatten.casual.jca.inbound.handler;

import se.kodarkatten.casual.api.CasualRuntimeException;

public class HandlerException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public HandlerException(String message)
    {
        super( message );
    }

    public HandlerException(Throwable t )
    {
        super( t );
    }

    public HandlerException(String message, Throwable t )
    {
        super( message, t );
    }


}
