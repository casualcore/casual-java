package se.kodarkatten.casual.jca.inbound.handler;

public class CasualHandlerException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public CasualHandlerException(String s)
    {
        super( s );
    }
}
