/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.inbound.startup;

import se.laz.casual.api.service.CasualService;
import se.laz.casual.config.Mode;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;

@Stateless
@Remote(TriggerService.class)
public class TriggerServiceImpl implements TriggerService
{
    @CasualService( name = Mode.Constants.TRIGGER_SERVICE, category = "internal" )
    @Override
    public void noOp()
    {
        /**
         * This method should never be called.
         * It's only purpose is to add a casual service to the inbound service registry upon deployment
         * that, when inbound startup mode is set to `trigger`, can initiate the delayed start
         * of the inbound server.
         */
    }
}
