/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.service.extension;

import se.laz.casual.spi.Priority;

public class DefaultServiceHandlerExtension implements ServiceHandlerExtension
{
    @Override
    public Priority getPriority()
    {
        return Priority.LEVEL_9;
    }

    @Override
    public boolean canHandle( String name )
    {
        return true;
    }
}
