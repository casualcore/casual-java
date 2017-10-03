package se.kodarkatten.casual.api;

public class CasualRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public CasualRuntimeException( )
    {
        super();
    }

    public CasualRuntimeException( String message )
    {
        super(  message );
    }

    public CasualRuntimeException( Throwable t )
    {
        super( t );
    }

    public CasualRuntimeException( String message, Throwable t )
    {
        super( message, t );
    }



}
