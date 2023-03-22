/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inbound.handler.test;

import se.laz.casual.jca.inbound.handler.service.extension.ServiceHandlerExtension;
import se.laz.casual.spi.Priority;

public class TestCasualServiceHandlerExtensionLowestPriority implements ServiceHandlerExtension
{
    @Override
    public boolean canHandle(String name)
    {
        return name.equals( TestHandler.class.getName());
    }

    @Override
    public Priority getPriority()
    {
        return Priority.LEVEL_0;
    }
}
