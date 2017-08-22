package se.kodarkatten.casual.network.connection;

import javax.ejb.ApplicationException;

/**
 * @author jone
 */
@ApplicationException
public class CasualStartupException extends RuntimeException
{
    public CasualStartupException(Throwable cause)
    {
        super(cause);
    }
}
