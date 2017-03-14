package se.kodarkatten.casual.network.messages.exceptions;

/**
 * Created by aleph on 2017-02-23.
 */
public final class CasualTransportException extends RuntimeException
{
    private static final long serialVersionUID = 1l;
    public CasualTransportException(String msg)
    {
        super(msg);
    }
    public CasualTransportException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
