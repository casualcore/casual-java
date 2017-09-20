package se.kodarkatten.casual.network.connection;

import javax.ejb.ApplicationException;

/**
 * Created by jone on 2017-04-26.
 */
@ApplicationException
public class CasualConnectionException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    public CasualConnectionException(Throwable t)
    {
        super(t);
    }
    public CasualConnectionException(String msg)
    {
        super(msg);
    }
}
