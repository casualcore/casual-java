/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api;

public class CasualTypeException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualTypeException(final String msg)
    {
        super(msg);
    }
    public CasualTypeException(final Throwable t)
    {
        super(t);
    }
    public CasualTypeException(final String msg, final Throwable t)
    {
        super(msg, t);
    }
}
