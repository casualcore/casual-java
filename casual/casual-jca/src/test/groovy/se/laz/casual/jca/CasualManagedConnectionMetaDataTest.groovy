/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Shared
import spock.lang.Specification

class CasualManagedConnectionMetaDataTest extends Specification
{
    @Shared
    CasualManagedConnectionMetaData instance

    def setup()
    {
        instance = new CasualManagedConnectionMetaData()
    }

    def "GetEISProductName"()
    {
        expect:
        instance.getEISProductName() != null
    }

    def "GetEISProductVersion"()
    {
        expect:
        instance.getEISProductVersion() != null
    }

    def "GetMaxConnections"()
    {
        expect:
        instance.getMaxConnections() == 0
    }

    def "GetUserName"()
    {
        expect:
        instance.getUserName() != null
    }
}
