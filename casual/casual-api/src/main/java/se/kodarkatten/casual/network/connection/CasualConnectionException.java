package se.kodarkatten.casual.network.connection;

import se.kodarkatten.casual.api.CasualRuntimeException;

import javax.ejb.ApplicationException;

/**
 * Created by jone on 2017-04-26.
 */
@ApplicationException(rollback = true)
public class CasualConnectionException extends CasualRuntimeException
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
