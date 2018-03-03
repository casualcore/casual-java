/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.flags

import spock.lang.Specification

import javax.transaction.xa.XAResource

/**
 * Created by aleph on 2017-03-31.
 */
class XAFlagsTest extends Specification
{
    def "check max value"()
    {
        setup:
        Flag.Builder builder = new Flag.Builder(XAFlags.TMONEPHASE.value)
        when:
        int flagValue = builder.or(XAFlags.TMENDRSCAN)
                               .or(XAFlags.TMFAIL)
                               .or(XAFlags.TMJOIN)
                               .or(XAFlags.TMRESUME)
                               .or(XAFlags.TMSTARTRSCAN)
                               .or(XAFlags.TMSUCCESS)
                               .or(XAFlags.TMSUSPEND)
                               .build().getFlagValue()
        then:
        flagValue == maxFlagValue()
    }

    def "test set, clear and isSet"()
    {
        setup:
        Flag<XAFlags> f = Flag.of().setFlag(XAFlags.TMJOIN)
                                   .setFlag(XAFlags.TMSUCCESS)
        when:
        f.clearFlag(XAFlags.TMJOIN)
        f.setFlag(XAFlags.TMONEPHASE)
        then:
        !f.isSet(XAFlags.TMJOIN)
        f.isSet(XAFlags.TMSUCCESS)
        f.isSet(XAFlags.TMONEPHASE)
    }

    def "values should match the values in the specification"()
    {
        setup:
        long maxFlagValue = maxFlagValue()
        when:
        long maxFlagValueFromXASpec = maxFlagValueFromXASpec()
        then:
        maxFlagValue == maxFlagValueFromXASpec
    }

    def maxFlagValueFromXASpec()
    {
        return  0x40000000l | 0x00800000l | 0x20000000l |
                0x00200000l | 0x08000000l | 0x01000000l |
                0x04000000l | 0x02000000l
    }

    int maxFlagValue()
    {
        return  XAResource.TMONEPHASE | XAResource.TMENDRSCAN | XAResource.TMFAIL |
                XAResource.TMJOIN     | XAResource.TMRESUME   | XAResource.TMSTARTRSCAN |
                XAResource.TMSUCCESS  | XAResource.TMSUSPEND
    }
}
