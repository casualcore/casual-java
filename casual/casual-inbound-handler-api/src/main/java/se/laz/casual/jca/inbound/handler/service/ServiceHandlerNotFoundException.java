/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service;

import se.laz.casual.jca.inbound.handler.HandlerException;

public class ServiceHandlerNotFoundException extends HandlerException
{
    private static final long serialVersionUID = 1L;

    public ServiceHandlerNotFoundException(String message)
    {
        super( message );
    }

    public ServiceHandlerNotFoundException(Throwable t )
    {
        super( t );
    }

    public ServiceHandlerNotFoundException(String message, Throwable t )
    {
        super( message, t );
    }


}
