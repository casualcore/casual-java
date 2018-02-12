package se.kodarkatten.casual.jca.inbound.handler.service;

import se.kodarkatten.casual.jca.inbound.handler.HandlerException;

public class ServiceHandlerNotFoundException extends HandlerException
{
    private static final long serialVersionUID = 1L;

    public ServiceHandlerNotFoundException(String message)
    {
        super( message );
    }

    public ServiceHandlerNotFoundException(Throwable t )
    {
        super( t );
    }

    public ServiceHandlerNotFoundException(String message, Throwable t )
    {
        super( message, t );
    }


}
