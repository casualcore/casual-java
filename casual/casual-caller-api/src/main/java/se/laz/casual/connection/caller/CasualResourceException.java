/*
 * Copyright (c) 2017 - 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.CasualRuntimeException;

public class CasualResourceException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1l;
    public CasualResourceException(String msg)
    {
        super(msg);
    }
    public CasualResourceException(Throwable t)
    {
        super(t);
    }
    public CasualResourceException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
