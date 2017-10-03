package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.CasualRuntimeException;

public class CasualResourceAdapterException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;

    public CasualResourceAdapterException( )
    {
        super();
    }

    public CasualResourceAdapterException( String message )
    {
        super(  message );
    }

    public CasualResourceAdapterException( Throwable t )
    {
        super( t );
    }

    public CasualResourceAdapterException( String message, Throwable t )
    {
        super( message, t );
    }

}
