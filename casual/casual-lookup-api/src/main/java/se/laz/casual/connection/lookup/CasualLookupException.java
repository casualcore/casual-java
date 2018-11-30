/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.lookup;

import se.laz.casual.api.CasualRuntimeException;

public class CasualLookupException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1l;
    public CasualLookupException(String msg)
    {
        super(msg);
    }
    public CasualLookupException(Throwable t)
    {
        super(t);
    }
    public CasualLookupException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
