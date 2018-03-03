/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer.fielded;

import se.laz.casual.api.service.CasualService;
import se.laz.casual.api.service.CasualServiceJndiName;

@CasualServiceJndiName("se.laz.casual.test.Service")
public interface SimpleService
{
    @CasualService(name="TestEcho" )
    SimpleObject echo( SimpleObject message);
}
