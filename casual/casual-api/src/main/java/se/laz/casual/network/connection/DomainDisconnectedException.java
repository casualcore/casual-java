/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.connection;

import se.laz.casual.api.CasualRuntimeException;

public class DomainDisconnectedException extends CasualRuntimeException
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
