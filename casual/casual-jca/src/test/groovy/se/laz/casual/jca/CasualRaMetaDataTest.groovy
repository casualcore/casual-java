/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Shared
import spock.lang.Specification

class CasualRaMetaDataTest extends Specification
{
    @Shared CasualRaMetaData instance

    def setup()
    {
       instance = new CasualRaMetaData()
    }

    def "GetAdapterVersion is not null."()
    {
        expect:
        instance.getAdapterVersion() != null
    }

    def "GetAdapterVendorName is not null."()
    {
        expect:
        instance.getAdapterVendorName() != null
    }

    def "GetAdapterName is not null."()
    {
        expect:
        instance.getAdapterName() != null
    }

    def "GetAdapterShortDescription is not null."()
    {
        expect:
        instance.getAdapterShortDescription() != null
    }

    def "GetSpecVersion is not null."()
    {
        expect:
        instance.getSpecVersion() == "1.7"
    }

    def "GetInteractionSpecsSupported"()
    {
        expect:
        instance.getInteractionSpecsSupported() == new String[0]
    }

    def "SupportsExecuteWithInputAndOutputRecord"()
    {
        expect:
        !instance.supportsExecuteWithInputAndOutputRecord()
    }

    def "SupportsExecuteWithInputRecordOnly"()
    {
        expect:
        !instance.supportsExecuteWithInputRecordOnly()
    }

    def "SupportsLocalTransactionDemarcation"()
    {
        expect:
        !instance.supportsLocalTransactionDemarcation()
    }
}
