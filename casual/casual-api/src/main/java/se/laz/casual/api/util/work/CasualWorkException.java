/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util.work;

import se.laz.casual.api.CasualRuntimeException;

public class CasualWorkException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualWorkException(String message, Throwable t)
    {
        super(message, t);
    }
}
