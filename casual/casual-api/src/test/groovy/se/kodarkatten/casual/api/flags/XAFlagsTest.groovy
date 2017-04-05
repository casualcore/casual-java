package se.kodarkatten.casual.api.flags

import spock.lang.Specification
import  se.kodarkatten.casual.api.flags.XAFlags


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
        long flagValue = builder
                               .or(XAFlags.TMASYNC)
                               .or(XAFlags.TMENDRSCAN)
                               .or(XAFlags.TMFAIL)
                               .or(XAFlags.TMJOIN)
                               .or(XAFlags.TMMIGRATE)
                               .or(XAFlags.TMMULTIPLE)
                               .or(XAFlags.TMNOWAIT)
                               .or(XAFlags.TMRESUME)
                               .or(XAFlags.TMSTARTRSCAN)
                               .or(XAFlags.TMSUCCESS)
                               .or(XAFlags.TMSUSPEND)
                               .build().getFlagValue()
        then:
        flagValue == maxFlagValue()
    }

    def long maxFlagValue()
    {
        return  0x80000000L | 0x40000000L | 0x20000000L |
                0x10000000L | 0x08000000L | 0x04000000L |
                0x02000000L | 0x01000000L | 0x00800000L |
                0x00400000L | 0x00200000L | 0x00100000L
    }
}
