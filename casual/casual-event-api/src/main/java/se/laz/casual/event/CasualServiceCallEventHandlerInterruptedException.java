/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event;

import se.laz.casual.api.CasualRuntimeException;

public class CasualServiceCallEventHandlerInterruptedException extends CasualRuntimeException
{
    private static final long serialVersionUID = 1L;
    public CasualServiceCallEventHandlerInterruptedException(String message)
    {
        super(message);
    }
}