/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.json;

import se.laz.casual.api.CasualRuntimeException;

/**
 * Exception that is used if anything unexpected occurs during lookup
 */
public class CasualFieldedLookupException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualFieldedLookupException(final String msg)
    {
        super(msg);
    }
    public CasualFieldedLookupException(final Throwable t)
    {
        super(t);
    }
    public CasualFieldedLookupException(final String msg, final Throwable t)
    {
        super(msg, t);
    }
}
