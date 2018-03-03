/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.internal.server.work

import se.laz.casual.internal.work.WorkContextFactory
import spock.lang.Specification

import javax.resource.spi.work.HintsContext
import javax.resource.spi.work.WorkContext

class WorkContextFactoryTest extends Specification
{
    def "Get WorkContext returns hint for long running."()
    {
        when:
        List<WorkContext> context = WorkContextFactory.createLongRunningContext()
        HintsContext c = (HintsContext)context.get( 0 )

        then:
        context.size() == 1
        c.getHints().size() == 1
        c.getHints().get( HintsContext.LONGRUNNING_HINT ) == Boolean.TRUE
    }
}
