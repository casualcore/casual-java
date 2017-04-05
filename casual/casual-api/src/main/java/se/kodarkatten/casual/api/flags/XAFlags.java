package se.kodarkatten.casual.api.flags;

import se.kodarkatten.casual.api.flags.internal.CasualFlag;

/**
 * Created by aleph on 2017-03-30.
 */
public enum XAFlags implements CasualFlag
{
    TMNOFLAGS(0),
    TMASYNC(0x80000000l),
    TMONEPHASE(0x40000000l),
    TMFAIL(0x20000000l),
    TMNOWAIT(0x10000000l),
    TMRESUME(0x08000000l),
    TMSUCCESS(0x04000000l),
    TMSUSPEND(0x02000000l),
    TMSTARTRSCAN(0x01000000l),
    TMENDRSCAN(0x00800000l),
    TMMULTIPLE(0x00400000l),
    TMJOIN(0x00200000l),
    TMMIGRATE(0x00100000l);

    final long v;
    XAFlags(long v)
    {
        this.v = v;
    }

    @Override
    public long getValue()
    {
        return v;
    }
}
