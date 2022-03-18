/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.connection;

import se.laz.casual.api.CasualRuntimeException;

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

    public CasualConnectionException(String message, Throwable t)
    {
        super(message, t);
    }

    public CasualConnectionException(String msg)
    {
        super(msg);
    }
}
