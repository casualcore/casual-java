package se.laz.casual.network.connection;

public class DomainDisconnectedException extends CasualConnectionException
{
    private static final long serialVersionUID = 1L;
    public DomainDisconnectedException(Throwable t)
    {
        super(t);
    }

    public DomainDisconnectedException(String message, Throwable t)
    {
        super(message, t);
    }

    public DomainDisconnectedException(String msg)
    {
        super(msg);
    }
}
